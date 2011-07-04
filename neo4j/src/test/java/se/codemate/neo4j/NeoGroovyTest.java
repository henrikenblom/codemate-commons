package se.codemate.neo4j;

import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NeoGroovyTest {

    private EmbeddedGraphDatabase neo;
    private NeoSearch neoSearch;
    private NeoGroovy neoGroovy;

    @BeforeClass(alwaysRun = true)
    @Parameters({"search.neo"})
    public void setUp(String path) throws Exception {
        neo = new EmbeddedGraphDatabase(path);
        neoSearch = new NeoSearch(neo);
        neoGroovy = new NeoGroovy(neo, neoSearch);
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
    @Parameters({"groovy.file"})
    public void testGroovy(String file) throws IOException, ParseException, InterruptedException {

        Transaction tx = neo.beginTx();

        try {

            List<PropertyContainer> containers = neoGroovy.evaluate(new File(file));

            for (PropertyContainer container : containers) {

                if (Node.class.isInstance(container)) {
                    System.out.println("Node " + ((Node) container).getId());
                } else if (Relationship.class.isInstance(container)) {
                    Relationship relationship = (Relationship) container;
                    System.out.println("Relationship " + relationship.getId() + " " + relationship.getStartNode().getId() + "->" + relationship.getEndNode().getId());
                }

                for (String key : container.getPropertyKeys()) {
                    System.out.println("   " + key + ":" + container.getProperty(key, null));
                }

            }

            tx.success();
        } finally {
            tx.finish();
        }

        Thread.sleep(100);

    }

}
