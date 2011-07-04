package se.codemate.neo4j;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XStreamNodeConverter implements Converter {

    public static String NODE_ID_MAP_KEY = "NodeIdMap";

    private GraphDatabaseService neo;
    private Mapper mapper;

    private Map<Long, Long> nodeIdMap = new HashMap<Long, Long>();

    public XStreamNodeConverter(Mapper mapper) {
        this(null, mapper);
    }

    public XStreamNodeConverter(GraphDatabaseService neo, Mapper mapper) {
        this.neo = neo;
        this.mapper = mapper;
    }

    public void setNeoService(GraphDatabaseService neo) {
        this.neo = neo;
    }

    public boolean canConvert(Class type) {
        return Node.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshal(source, (ExtendedHierarchicalStreamWriter) writer, context);
    }

    public void marshal(Object source, ExtendedHierarchicalStreamWriter writer, MarshallingContext context) {

        GraphDatabaseService neo;
        if (context.get(XStreamEmbeddedNeoConverter.NEO_SERVICE_KEY) == null) {
            neo = this.neo;
        } else {
            neo = (GraphDatabaseService) context.get(XStreamEmbeddedNeoConverter.NEO_SERVICE_KEY);
            this.neo = neo;
        }

        Node node = (Node) source;

        writer.startNode("id", Long.class);
        writer.setValue(Long.toString(node.getId()));
        writer.endNode(); // id

        Transaction tx = neo.beginTx();
        try {
            boolean hasProperties = false;
            for (String key : node.getPropertyKeys()) {
                if (!hasProperties) {
                    writer.startNode("properties");
                    hasProperties = true;
                }
                Object value = node.getProperty(key);
                writer.startNode(key);
                writer.startNode("class", String.class);
                writer.setValue(mapper.serializedClass(value.getClass()));
                writer.endNode(); // class
                writer.startNode("value", value.getClass());
                context.convertAnother(value);
                writer.endNode(); // value
                writer.endNode(); // key
            }
            if (hasProperties) {
                writer.endNode(); // properties
            }
            tx.success();
        } finally {
            tx.finish();
        }

    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        context.put(NODE_ID_MAP_KEY, nodeIdMap);

        GraphDatabaseService neo;
        if (context.get(XStreamEmbeddedNeoConverter.NEO_SERVICE_KEY) == null) {
            neo = this.neo;
        } else {
            neo = (GraphDatabaseService) context.get(XStreamEmbeddedNeoConverter.NEO_SERVICE_KEY);
            this.neo = neo;
        }

        Node node;

        Transaction tx = neo.beginTx();
        try {

            reader.moveDown(); // id

            try {
                node = neo.getNodeById(Long.parseLong(reader.getValue()));
                nodeIdMap.put(Long.parseLong(reader.getValue()), node.getId());
            } catch (NotFoundException exception) {
                node = neo.createNode();
                nodeIdMap.put(Long.parseLong(reader.getValue()), node.getId());
            } catch (NumberFormatException exception) {
                node = neo.createNode();
                nodeIdMap.put(node.getId(), node.getId());
            }

            reader.moveUp(); // id

            Set<String> keys = NeoUtils.getKeysSet(node);

            if (reader.hasMoreChildren()) {

                reader.moveDown(); // properties

                while (reader.hasMoreChildren()) {

                    reader.moveDown(); // key

                    String key = reader.getNodeName();

                    reader.moveDown(); // class
                    Class type = mapper.realClass(reader.getValue());
                    reader.moveUp(); // class

                    reader.moveDown(); // value
                    Object value = context.convertAnother(context.currentObject(), type);
                    reader.moveUp(); // value

                    reader.moveUp(); // key

                    node.setProperty(key, value);
                    keys.remove(key);

                }

                reader.moveUp(); // properties

            }

            for (String key : keys) {
                node.removeProperty(key);
            }

            tx.success();
        } finally {
            tx.finish();
        }

        return node;

    }

}
