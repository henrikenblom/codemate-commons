package se.codemate.neo4j;

import com.thoughtworks.xstream.XStream;
import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class NeoSearchTest {

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
    public void testSearch() throws IOException, ParseException {

        Transaction tx = neo.beginTx();

        try {

            List<PropertyContainer> containers = neoSearch.getPropertyContainers("name:d*B");
            //List<PropertyContainer> containers = neoSearch.getPropertyContainers("_relType:\"CONNECTED_TO\" AND _endNodeID:11");

            for (PropertyContainer container : containers) {

                if (Node.class.isInstance(container)) {
                    System.out.println("Node " + ((Node) container).getId());
                } else {
                    Relationship relationship = (Relationship) container;
                    System.out.println("Relationship " + relationship.getId()+" "+relationship.getStartNode().getId()+"->"+relationship.getEndNode().getId());
                }

                for (String key : container.getPropertyKeys()) {
                    System.out.println("   " + key + ":" + container.getProperty(key, null));
                }

            }

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
