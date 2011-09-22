package se.codemate.neo4j;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.util.TreeSet;

public class XStreamEmbeddedNeoConverter implements Converter {

    public static String NEO_SERVICE_KEY = "NeoService";

    private EmbeddedGraphDatabase embeddedNeo;

    public XStreamEmbeddedNeoConverter() {
        this(null);
    }

    public XStreamEmbeddedNeoConverter(EmbeddedGraphDatabase embeddedNeo) {
        this.embeddedNeo = embeddedNeo;
    }

    public boolean canConvert(Class type) {
        return EmbeddedGraphDatabase.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshal(source, (ExtendedHierarchicalStreamWriter) writer, context);
    }

    public void marshal(Object source, ExtendedHierarchicalStreamWriter writer, MarshallingContext context) {

        final TreeSet<Long> completedNodes = new TreeSet<Long>();
        final TreeSet<Long> nodeQueue = new TreeSet<Long>();
        final TreeSet<Long> relationshipQueue = new TreeSet<Long>();

        final EmbeddedGraphDatabase embeddedNeo = (EmbeddedGraphDatabase) source;

        final Transaction tx = embeddedNeo.beginTx();

        context.put(NEO_SERVICE_KEY, embeddedNeo);

        try {

            writer.startNode("storeDir", String.class);
            writer.setValue(embeddedNeo.getConfig().getTxModule().getTxLogDirectory());
            writer.endNode(); // storeDir

            nodeQueue.add(embeddedNeo.getReferenceNode().getId());

            writer.startNode("nodes");
            while (!nodeQueue.isEmpty()) {
                long id = nodeQueue.pollFirst();
                if (!completedNodes.contains(id)) {
                    Node currentNode = embeddedNeo.getNodeById(id);
                    writer.startNode("node", Node.class);
                    context.convertAnother(currentNode);
                    writer.endNode(); // node
                    completedNodes.add(id);
                    for (Relationship relationship : currentNode.getRelationships()) {
                        long otherId = relationship.getOtherNode(currentNode).getId();
                        if (!completedNodes.contains(otherId)) {
                            nodeQueue.add(otherId);
                        }
                        relationshipQueue.add(relationship.getId());
                    }
                }
            }
            writer.endNode(); // nodes

            writer.startNode("relationships");
            while (!relationshipQueue.isEmpty()) {
                writer.startNode("relationship", Relationship.class);
                context.convertAnother(embeddedNeo.getRelationshipById(relationshipQueue.pollFirst()));
                writer.endNode(); // relationship
            }
            writer.endNode(); // relationships

        } finally {
            tx.finish();
        }

    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        EmbeddedGraphDatabase embeddedNeo;

        reader.moveDown(); // storeDir or nodes

        if ("storeDir".equals(reader.getNodeName())) {
            embeddedNeo = new EmbeddedGraphDatabase(reader.getValue());
            reader.moveUp(); //storeDir
            reader.moveDown(); //nodes
        } else {
            embeddedNeo = this.embeddedNeo;
        }

        context.put(NEO_SERVICE_KEY, embeddedNeo);

        final Transaction tx = embeddedNeo.beginTx();

        try {

            while (reader.hasMoreChildren()) {
                reader.moveDown(); // node
                context.convertAnother(context.currentObject(), Node.class);
                reader.moveUp(); //node
            }
            reader.moveUp(); //nodes

            reader.moveDown(); // relationships
            while (reader.hasMoreChildren()) {
                reader.moveDown(); // relationship
                context.convertAnother(context.currentObject(), Relationship.class);
                reader.moveUp(); // relationship
            }
            reader.moveUp(); //relationships

            tx.success();

        } finally {
            tx.finish();
        }

        return embeddedNeo;

    }

}
