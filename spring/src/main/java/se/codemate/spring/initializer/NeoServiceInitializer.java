package se.codemate.spring.initializer;

import com.thoughtworks.xstream.XStream;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import se.codemate.neo4j.XStreamEmbeddedNeoConverter;
import se.codemate.neo4j.XStreamNodeConverter;
import se.codemate.neo4j.XStreamRelationshipConverter;
import se.codemate.neo4j.NeoSearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;

public class NeoServiceInitializer implements ApplicationContextAware, InitializingBean {

    private String configuration;

    private ApplicationContext applicationContext;

    private EmbeddedGraphDatabase neoService;

    private NeoSearch neoSearch;

    private boolean forceInitialization;

    private boolean indexGraph;

    public NeoServiceInitializer(EmbeddedGraphDatabase neoService) {
        this.neoService = neoService;
    }

    public void setNeoSearch(NeoSearch neoSearch) {
        this.neoSearch = neoSearch;
    }

    @Required
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public void setForceInitialization(boolean forceInitialization) {
        this.forceInitialization = forceInitialization;
    }

    public void setIndexGraph(boolean indexGraph) {
        this.indexGraph = indexGraph;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {

        Transaction tx = neoService.beginTx();

        try {

            Node node = neoService.getNodeById(0);

            boolean initialized = (Boolean) node.getProperty("initialized", false);

            if (forceInitialization || !initialized) {

                Resource resource = applicationContext.getResource(configuration);
                Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

                XStream xstream = new XStream();
                xstream.setMode(XStream.NO_REFERENCES);

                xstream.alias("embeddedNeo", EmbeddedGraphDatabase.class);

                xstream.alias("node", Node.class);
                xstream.alias("node", Class.forName("org.neo4j.kernel.impl.core.NodeImpl"));
                xstream.alias("node", Class.forName("org.neo4j.kernel.impl.core.NodeProxy"));

                xstream.alias("relationship", Relationship.class);
                xstream.alias("relationship", Class.forName("org.neo4j.kernel.impl.core.RelationshipImpl"));
                xstream.alias("relationship", Class.forName("org.neo4j.kernel.impl.core.RelationshipProxy"));

                xstream.registerConverter(new XStreamEmbeddedNeoConverter(neoService));
                xstream.registerConverter(new XStreamNodeConverter(xstream.getMapper()));
                xstream.registerConverter(new XStreamRelationshipConverter(xstream.getMapper()));

                ObjectInputStream in = xstream.createObjectInputStream(reader);
                in.readObject();
                reader.close();

                node.setProperty("name", applicationContext.getDisplayName());
                node.setProperty("initialized", true);


            }

            if (indexGraph && neoSearch != null) {
                neoSearch.indexGraph();
            }

            tx.success();

        } finally {
            tx.finish();
        }

    }

}

