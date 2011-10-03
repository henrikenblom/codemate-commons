package se.codemate.neo4j;

import com.thoughtworks.xstream.XStream;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class XStreamHelper {

    public static XStream createXStreamInstance() throws Exception {

        XStream xstream = new XStream();
        xstream.setMode(XStream.NO_REFERENCES);

        xstream.alias("embeddedNeo", EmbeddedGraphDatabase.class);

        xstream.alias("node", Node.class);
        xstream.alias("node", Class.forName("org.neo4j.kernel.impl.core.NodeImpl"));
        xstream.alias("node", Class.forName("org.neo4j.kernel.impl.core.NodeProxy"));

        xstream.alias("relationship", Relationship.class);
        xstream.alias("relationship", Class.forName("org.neo4j.kernel.impl.core.RelationshipImpl"));
        xstream.alias("relationship", Class.forName("org.neo4j.kernel.impl.core.RelationshipProxy"));

        xstream.registerConverter(new XStreamEmbeddedNeoConverter());
        xstream.registerConverter(new XStreamNodeConverter(xstream.getMapper()));
        xstream.registerConverter(new XStreamRelationshipConverter(xstream.getMapper()));

        return xstream;

    }

}
