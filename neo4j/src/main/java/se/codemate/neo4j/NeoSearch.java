package se.codemate.neo4j;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeoSearch {

    private static Logger log = Logger.getLogger(NeoSearch.class);

    private static Pattern digitsPattern = Pattern.compile("(\\S+):\\[?(-?\\d+\\.?\\d*)(\\s+|$)|to\\s+(-?\\d+\\.?\\d*)\\]");
    private static Pattern datePattern = Pattern.compile(":\\[?(\\d{1,2}/\\d{1,2}/\\d{2,4}|\\d{2,4}-\\d{1,2}-\\d{1,2})(\\s+|$)|to\\s+(\\d{1,2}/\\d{1,2}/\\d{2,4}|\\d{2,4}-\\d{1,2}-\\d{1,2})]");
    private static Pattern andOrPattern = Pattern.compile("\\s+(and|or)\\s+", Pattern.CASE_INSENSITIVE);

    private static SimpleDateFormat slashDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static SimpleDateFormat hyphenDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String NODE_ID_LABEL = "_nodeID";
    public static String RELATIONSHIP_ID_LABEL = "_relID";
    public static String RELATIONSHIP_TYPE_LABEL = "_relType";
    public static String RELATIONSHIP_START_NODE_LABEL = "_startNodeID";
    public static String RELATIONSHIP_END_NODE_LABEL = "_endNodeID";
    public static String PROPERTY_FIELD_LABEL = "_property";

    private class NodeHitCollector extends HitCollector {

        private List<Node> nodes = new LinkedList<Node>();

        public void collect(int doc, float score) {
            try {
                Document document = reader.document(doc);
                String nodeId = document.get(NODE_ID_LABEL);
                if (nodeId != null) {
                    long id = Long.parseLong(nodeId);
                    nodes.add(neo.getNodeById(id));
                }
            } catch (NotFoundException exception) {
                // no-op
            } catch (IOException exception) {
                // no-op
            }
        }

        public List<Node> getNodes() {
            return nodes;
        }

    }

    private class RelationshipHitCollector extends HitCollector {

        private List<Relationship> relationships = new LinkedList<Relationship>();

        public void collect(int doc, float score) {
            try {
                Document document = reader.document(doc);
                String relationshipId = document.get(RELATIONSHIP_ID_LABEL);
                if (relationshipId != null) {
                    long id = Long.parseLong(relationshipId);
                    relationships.add(neo.getRelationshipById(id));
                }
            } catch (NotFoundException exception) {
                // no-op
            } catch (IOException exception) {
                // no-op
            }
        }

        public List<Relationship> getRelationships() {
            return relationships;
        }

    }

    private class PropertyContainerHitCollector extends HitCollector {

        private List<PropertyContainer> containers = new LinkedList<PropertyContainer>();

        public void collect(int doc, float score) {
            try {
                Document document = reader.document(doc);
                boolean isNode = true;
                String containerId = document.get(NODE_ID_LABEL);
                if (containerId == null) {
                    isNode = false;
                    containerId = document.get(RELATIONSHIP_ID_LABEL);
                }
                if (containerId != null) {
                    long id = Long.parseLong(containerId);
                    containers.add(isNode ? neo.getNodeById(id) : neo.getRelationshipById(id));
                }
            } catch (NotFoundException exception) {
                // no-op
            } catch (IOException exception) {
                // no-op
            }
        }

        public List<PropertyContainer> getContainers() {
            return containers;
        }

    }

    private GraphDatabaseService neo;

    private Directory directory;

    private IndexReader reader;
    private IndexWriter writer;
    private IndexSearcher searcher;

    private QueryParser queryParser;

    private Field.Store fieldStoreFlag = Field.Store.NO;

    private Field.TermVector fieldTermVectorFlag = Field.TermVector.NO;

    public NeoSearch(GraphDatabaseService neo) throws IOException {
        this(neo, "content", "OR");
    }

    public NeoSearch(GraphDatabaseService neo, String defaultField, String defaultOperator) throws IOException {

        if (!EmbeddedGraphDatabase.class.isInstance(neo)) {
            throw new IOException("Only services of type EmbeddedNeo are supported");
        }

        this.neo = neo;

        EmbeddedGraphDatabase embeddedNeo = ((EmbeddedGraphDatabase) neo);

        String path = embeddedNeo.getConfig().getTxModule().getTxLogDirectory() + File.separator + "lucene";
        directory = NIOFSDirectory.getDirectory(path);
        directory.clearLock("write.lock");

        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        analyzer.addAnalyzer(NODE_ID_LABEL, new KeywordAnalyzer());
        analyzer.addAnalyzer(RELATIONSHIP_ID_LABEL, new KeywordAnalyzer());
        analyzer.addAnalyzer(RELATIONSHIP_TYPE_LABEL, new KeywordAnalyzer());
        analyzer.addAnalyzer(RELATIONSHIP_START_NODE_LABEL, new KeywordAnalyzer());
        analyzer.addAnalyzer(RELATIONSHIP_END_NODE_LABEL, new KeywordAnalyzer());
        analyzer.addAnalyzer(PROPERTY_FIELD_LABEL, new KeywordAnalyzer());

        try {
            writer = new IndexWriter(directory, analyzer, false, IndexWriter.MaxFieldLength.UNLIMITED);
        } catch (FileNotFoundException excepeption) {
            writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
        }

        reader = IndexReader.open(directory);

        searcher = new IndexSearcher(reader);

        queryParser = new QueryParser(defaultField, analyzer);
        queryParser.setDateResolution(DateTools.Resolution.DAY);
        queryParser.setDefaultOperator("OR".equalsIgnoreCase(defaultOperator) ? QueryParser.Operator.OR : QueryParser.AND_OPERATOR);

        writer.optimize(false);

    }

    public void enableStoreFieldsAndTermVectors() {
        setFieldStoreFlag(Field.Store.YES);
        setTermVectorFlag(Field.TermVector.YES);
    }

    public void setFieldStoreFlag(Field.Store flag) {
        fieldStoreFlag = flag;
    }

    public void setTermVectorFlag(Field.TermVector flag) {
        fieldTermVectorFlag = flag;
    }

    public synchronized void indexTraverser(Traverser traverser) throws IOException {
        for (Node node : traverser) {
            indexNode(node);
            TraversalPosition position = traverser.currentPosition();
            Relationship relationship = position.lastRelationshipTraversed();
            if (relationship != null) {
                indexRelationship(relationship);
            }
        }
    }

    public synchronized void indexGraph() throws IOException {
        writer.deleteDocuments(new MatchAllDocsQuery());
        indexGraph(neo.getReferenceNode());
    }

    public synchronized void indexGraph(Node startNode) throws IOException {

        final TreeSet<Long> completedNodes = new TreeSet<Long>();
        final LinkedList<Long> nodeQueue = new LinkedList<Long>();
        final LinkedList<Long> relationshipQueue = new LinkedList<Long>();

        final Transaction tx = neo.beginTx();
        try {

            nodeQueue.add(startNode.getId());

            while (!nodeQueue.isEmpty()) {
                final long id = nodeQueue.removeFirst();
                if (!completedNodes.contains(id)) {
                    Node currentNode = neo.getNodeById(id);
                    indexNode(currentNode);
                    completedNodes.add(id);
                    for (Relationship relationship : currentNode.getRelationships()) {
                        final long otherId = relationship.getOtherNode(currentNode).getId();
                        if (!completedNodes.contains(otherId)) {
                            nodeQueue.addLast(otherId);
                        }
                        relationshipQueue.addLast(relationship.getId());
                    }
                }
            }

            while (!relationshipQueue.isEmpty()) {
                indexRelationship(neo.getRelationshipById(relationshipQueue.removeFirst()));
            }

        } finally {
            tx.finish();
        }

    }

    public synchronized void indexNode(Node node) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Indexing node " + node.getId());
        }
        writer.deleteDocuments(new Term(NODE_ID_LABEL, Long.toString(node.getId())));
        Document document = new Document();
        document.add(new Field(NODE_ID_LABEL, Long.toString(node.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, fieldTermVectorFlag));
        addProperties(document, node);
        writer.addDocument(document);
        writer.commit();
    }

    public synchronized void removeNode(Node node) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Removing node " + node.getId());
        }
        writer.deleteDocuments(new Term(NODE_ID_LABEL, Long.toString(node.getId())));
        writer.commit();
    }

    public synchronized void indexRelationship(Relationship relationship) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Indexing relationship " + relationship.getId());
        }
        writer.deleteDocuments(new Term(RELATIONSHIP_ID_LABEL, Long.toString(relationship.getId())));
        Document document = new Document();
        document.add(new Field(RELATIONSHIP_ID_LABEL, Long.toString(relationship.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, fieldTermVectorFlag));
        document.add(new Field(RELATIONSHIP_TYPE_LABEL, relationship.getType().name(), fieldStoreFlag, Field.Index.NOT_ANALYZED_NO_NORMS, fieldTermVectorFlag));
        document.add(new Field(RELATIONSHIP_START_NODE_LABEL, Long.toString(relationship.getStartNode().getId()), fieldStoreFlag, Field.Index.NOT_ANALYZED_NO_NORMS, fieldTermVectorFlag));
        document.add(new Field(RELATIONSHIP_END_NODE_LABEL, Long.toString(relationship.getEndNode().getId()), fieldStoreFlag, Field.Index.NOT_ANALYZED_NO_NORMS, fieldTermVectorFlag));
        addProperties(document, relationship);
        writer.addDocument(document);
        writer.commit();
    }

    public synchronized void removeRelationship(Relationship relationship) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Removing relationship " + relationship.getId());
        }
        writer.deleteDocuments(new Term(RELATIONSHIP_ID_LABEL, Long.toString(relationship.getId())));
        writer.commit();
    }

    private void addProperties(Document document, PropertyContainer propertyContainer) {
        final Transaction tx = neo.beginTx();
        try {
            for (String key : propertyContainer.getPropertyKeys()) {
                document.add(new Field(PROPERTY_FIELD_LABEL, key, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
                addValue(document, key, propertyContainer.getProperty(key));
            }
            tx.success();
        } finally {
            tx.finish();
        }
    }

    private void addValue(Document document, String key, Object value) {
        if (value != null) {
            if (Date.class.isInstance(value)) {
                document.add(new Field(key, DateTools.dateToString((Date) value, DateTools.Resolution.DAY), fieldStoreFlag, Field.Index.ANALYZED, fieldTermVectorFlag));
            } else if (Number.class.isInstance(value)) {
                Number number = (Number) value;
                long iee754 = Double.doubleToRawLongBits(number.doubleValue());
                String lexicalNumber = NumberTools.longToString(iee754 < 0 ? iee754 ^ 0x7fffffffffffffffL : iee754);
                document.add(new Field(key, lexicalNumber, fieldStoreFlag, Field.Index.ANALYZED, fieldTermVectorFlag));
            } else if (Collection.class.isInstance(value)) {
                Collection collection = (Collection) value;
                for (Object item : collection) {
                    addValue(document, key, item);
                }
            } else {
                document.add(new Field(key, value.toString(), fieldStoreFlag, Field.Index.ANALYZED, fieldTermVectorFlag));
            }
        }
    }

    public synchronized List<Node> getNodes(String query) throws IOException, ParseException {
        return getNodes(queryParser.parse(preProcessQuery(query)));
    }

    public synchronized List<Node> getNodes(Query query) throws IOException {
        refreshReader();
        NodeHitCollector collector = new NodeHitCollector();
        searcher.search(query, collector);
        return collector.getNodes();
    }

    public synchronized List<Relationship> getRelationships(String query) throws IOException, ParseException {
        return getRelationships(queryParser.parse(preProcessQuery(query)));
    }

    public synchronized List<Relationship> getRelationships(Query query) throws IOException {
        refreshReader();
        RelationshipHitCollector collector = new RelationshipHitCollector();
        searcher.search(query, collector);
        return collector.getRelationships();
    }

    public synchronized List<PropertyContainer> getPropertyContainers(String query) throws IOException, ParseException {
        return getPropertyContainers(queryParser.parse(preProcessQuery(query)));
    }

    public synchronized List<PropertyContainer> getPropertyContainers(Query query) throws IOException {
        refreshReader();
        PropertyContainerHitCollector collector = new PropertyContainerHitCollector();
        searcher.search(query, collector);
        return collector.getContainers();
    }

    private void refreshReader() throws IOException {
        if (!reader.isCurrent()) {
            IndexReader newReader = reader.reopen();
            if (newReader != reader) {
                reader.close();
                reader = newReader;
                searcher.close();
                searcher = new IndexSearcher(reader);
            }
        }
    }

    public void shutdown() {

        try {
            searcher.close();
        } catch (IOException exception) {
            // NO-OP
        }

        try {
            reader.close();
        } catch (IOException exception) {
            // NO-OP
        }

        try {
            writer.close();
        } catch (IOException exception) {
            // NO-OP
        } finally {
            try {
                if (IndexWriter.isLocked(directory)) {
                    IndexWriter.unlock(directory);
                }
                directory.close();
            } catch (IOException e) {
                // NO-OP
            }
        }

    }

    public static String preProcessQuery(String query) {

        StringBuilder builder = new StringBuilder(query);

        int deltaSum = 0;

        Matcher digitsMatcher = digitsPattern.matcher(query);
        while (digitsMatcher.find()) {
            String field = digitsMatcher.group(1);
            if (!(NODE_ID_LABEL.equals(field) ||
                    RELATIONSHIP_ID_LABEL.equals(field) ||
                    RELATIONSHIP_START_NODE_LABEL.equals(field) ||
                    RELATIONSHIP_END_NODE_LABEL.equals(field))) {
                for (int i = 1; i <= digitsMatcher.groupCount(); i++) {
                    if ((i == 2 || i == 4) && digitsMatcher.group(i) != null) {
                        long iee754 = Double.doubleToRawLongBits(Double.parseDouble(digitsMatcher.group(i)));
                        String s = NumberTools.longToString(iee754 < 0 ? iee754 ^ 0x7fffffffffffffffL : iee754);
                        int len = builder.length();
                        builder.replace(digitsMatcher.start(i) + deltaSum, digitsMatcher.end(i) + deltaSum, s);
                        deltaSum += builder.length() - len;

                    }
                }
            }
        }

        deltaSum = 0;

        Matcher dateMatcher = datePattern.matcher(builder.toString());
        while (dateMatcher.find()) {
            for (int i = 0; i <= dateMatcher.groupCount(); i++) {
                if ((i == 1 || i == 3) && dateMatcher.group(i) != null) {
                    try {
                        Date date = dateMatcher.group(i).indexOf('-') == -1 ? slashDateFormat.parse(dateMatcher.group(i)) : hyphenDateFormat.parse(dateMatcher.group(i));
                        String s = DateTools.dateToString(date, DateTools.Resolution.DAY);
                        int len = builder.length();
                        builder.replace(dateMatcher.start(i) + deltaSum, dateMatcher.end(i) + deltaSum, s);
                        deltaSum += builder.length() - len;
                    } catch (java.text.ParseException e) {
                        // no-op
                    }
                }
            }
        }

        deltaSum = 0;

        Matcher andOrMatcher = andOrPattern.matcher(builder.toString());
        while (andOrMatcher.find()) {
            for (int i = 0; i <= andOrMatcher.groupCount(); i++) {
                if (i == 1) {
                    builder.replace(andOrMatcher.start(i) + deltaSum, andOrMatcher.end(i) + deltaSum, andOrMatcher.group(i).toUpperCase());
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Pre Processed Query: " + builder);
        }

        return builder.toString();

    }


}
