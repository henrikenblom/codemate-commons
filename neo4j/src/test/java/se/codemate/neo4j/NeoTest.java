package se.codemate.neo4j;

import com.thoughtworks.xstream.XStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;

public class NeoTest {

    private XStream xstream;
    private EmbeddedGraphDatabase neo;
    private NeoSearch neoSearch;

    @BeforeClass(alwaysRun = true)
    @Parameters({"test.data.small"})
    public void setUp(String path) throws Exception {

        xstream = XStreamHelper.createXStreamInstance();

        ObjectInputStream in = xstream.createObjectInputStream(new FileInputStream(path));
        neo = (EmbeddedGraphDatabase) in.readObject();
        neoSearch = new NeoSearch(neo);
        neoSearch.indexGraph();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {

        if (neoSearch != null) {
            neoSearch.shutdown();
        }

        if (neo != null) {
            neo.shutdown();
            deleteDir(new File(neo.getConfig().getTxModule().getTxLogDirectory()));
        }

        System.out.flush();

    }

    @Test(groups = {"functest"})
    public void testExport() throws IOException, ParseException {

        Node node = neo.getReferenceNode();

        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(node);

        Set<Node> nodes = new HashSet<Node>();
        Set<Relationship> relationships = new HashSet<Relationship>();

        Transaction tx = neo.beginTx();

        while (!queue.isEmpty()) {
            Node currentNode = queue.removeFirst();
            if (nodes.add(currentNode)) {
                for (Relationship relationship : currentNode.getRelationships(Direction.OUTGOING)) {
                    if (relationships.add(relationship)) {
                        queue.add(relationship.getEndNode());
                    }
                }
            }
        }

        try {
            xstream.toXML(nodes, System.out);
            xstream.toXML(relationships, System.out);
        } finally {
            tx.success();
            tx.finish();
        }

    }

    @Test(groups = {"functest"})
    public void testSearch() throws IOException, ParseException {
        Transaction tx = neo.beginTx();
        try {

            List<Node> nodes = neoSearch.getNodes(new MatchAllDocsQuery());
            System.out.println("Graph contains " + nodes.size() + " nodes.");

            List<Relationship> relationships = neoSearch.getRelationships(new MatchAllDocsQuery());
            System.out.println("Graph contains " + relationships.size() + " relationships.");

            List<PropertyContainer> containers = neoSearch.getPropertyContainers(new MatchAllDocsQuery());
            System.out.println("Graph contains " + containers.size() + " containers.");

            ObjectOutputStream out = xstream.createObjectOutputStream(System.out, "neo4j");
            out.writeObject(neo);
            out.close();

            System.out.println("");

            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test(groups = {"functest"})
    public void testDelete() throws IOException, ParseException {
        Transaction tx = neo.beginTx();
        try {

            NeoUtils neoUtils = new NeoUtils(neo);

            System.out.println(NeoUtils.toValueList(neoSearch.getNodes("name:delete*"),"name"));

            neoUtils.deleteNode(neo.getNodeById(6));
            neoSearch.indexGraph();
            System.out.println(NeoUtils.toValueList(neoSearch.getNodes("name:delete*"),"name"));

            neoUtils.deleteNode(neo.getNodeById(9));
            neoSearch.indexGraph();
            System.out.println(NeoUtils.toValueList(neoSearch.getNodes("name:delete*"),"name"));

            tx.success();
        } finally {
            tx.finish();
        }
    }

    private static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDir(file);
            }
        }
        dir.delete();
    }

}