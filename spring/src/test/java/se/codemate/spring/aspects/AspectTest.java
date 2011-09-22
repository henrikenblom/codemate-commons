package se.codemate.spring.aspects;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

public class AspectTest {

    private static AbstractApplicationContext context;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("context.xml");
        context.registerShutdownHook();
        context.start();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        context.stop();
        EmbeddedGraphDatabase neoService = (EmbeddedGraphDatabase) context.getBean("neoService");
        neoService.shutdown();
    }

    @Test(groups = {"functest"})
    public void testAspect() throws Exception {
        GraphDatabaseService neoService = (GraphDatabaseService) context.getBean("neoService");
        Transaction transaction = neoService.beginTx();
        Node node = neoService.createNode();
        try {
            node.setProperty("Hello", "World");
        } finally {
            transaction.finish();
        }
    }

    @Test(groups = {"functest"})
    public void testObjectMapping() throws Exception {
        GraphDatabaseService neoService = (GraphDatabaseService) context.getBean("neoService");
        Transaction transaction = neoService.beginTx();
        Node node = neoService.createNode();
        try {
            node.setProperty("Date", new Date());
            node.setProperty("Long", 435634L);
            node.setProperty("Array", new String[]{"a", "b", "c"});
            node.removeProperty("Array");
            node.removeProperty("TS_CREATED");
            for (String key : node.getPropertyKeys()) {
                Object value = node.getProperty(key);
                System.out.println(key + ":" + value + "(" + value.getClass().getName() + ")");
            }
        } finally {
            transaction.finish();
        }
    }


}
