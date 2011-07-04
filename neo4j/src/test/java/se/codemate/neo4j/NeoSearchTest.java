package se.codemate.neo4j;

import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class NeoSearchTest {

    private EmbeddedGraphDatabase neo;
    private NeoSearch neoSearch;

    @BeforeClass(alwaysRun = true)
    @Parameters({"search.neo"})
    public void setUp(String path) throws Exception {
        neo = new EmbeddedGraphDatabase(path);
        neoSearch = new NeoSearch(neo);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {

        if (neoSearch != null) {
            neoSearch.shutdown();
        }

        if (neo != null) {
            neo.shutdown();
        }

        System.out.flush();

    }


    @Test(groups = {"functest"})
    public void testSearch() throws IOException, ParseException {

        Transaction tx = neo.beginTx();

        try {

            //List<PropertyContainer> containers = neoSearch.getPropertyContainers("name:A* AND nodeClass:organization");
            List<PropertyContainer> containers = neoSearch.getPropertyContainers("_relType:\"REPORTS_TO\" AND _endNodeID:18");

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

}
