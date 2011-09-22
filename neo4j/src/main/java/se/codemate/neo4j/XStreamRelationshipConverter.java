package se.codemate.neo4j;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.neo4j.graphdb.*;

import java.util.Map;
import java.util.Set;

public class XStreamRelationshipConverter implements Converter {

    private GraphDatabaseService neo;
    private Mapper mapper;

    public XStreamRelationshipConverter(Mapper mapper) {
        this(null, mapper);
    }

    public XStreamRelationshipConverter(GraphDatabaseService neo, Mapper mapper) {
        this.neo = neo;
        this.mapper = mapper;
    }

    public void setNeoService(GraphDatabaseService neo) {
        this.neo = neo;
    }

    public boolean canConvert(Class type) {
        return Relationship.class.isAssignableFrom(type);
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
        
        Relationship relationship = (Relationship) source;

        writer.startNode("id", Long.class);
        writer.setValue(Long.toString(relationship.getId()));
        writer.endNode(); // id

        writer.startNode("type", String.class);
        writer.setValue(relationship.getType().name());
        writer.endNode(); // type

        writer.startNode("startNode", Long.class);
        writer.setValue(Long.toString(relationship.getStartNode().getId()));
        writer.endNode(); // startNode

        writer.startNode("endNode", Long.class);
        writer.setValue(Long.toString(relationship.getEndNode().getId()));
        writer.endNode(); // endNode 

        Transaction tx = neo.beginTx();
        try {
            boolean hasProperties = false;
            for (String key : relationship.getPropertyKeys()) {
                if (!hasProperties) {
                    writer.startNode("properties");
                    hasProperties = true;
                }
                Object value = relationship.getProperty(key);
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

    @SuppressWarnings(value = "unchecked")
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        GraphDatabaseService neo;
        if (context.get(XStreamEmbeddedNeoConverter.NEO_SERVICE_KEY) == null) {
            neo = this.neo;
        } else {
            neo = (GraphDatabaseService) context.get(XStreamEmbeddedNeoConverter.NEO_SERVICE_KEY);
            this.neo = neo;
        }

        Map<Long, Long> nodeIdMap = (Map<Long, Long>) context.get(XStreamNodeConverter.NODE_ID_MAP_KEY);

        reader.moveDown(); // id
        String id = reader.getValue();
        reader.moveUp(); // id

        reader.moveDown(); // type
        SimpleRelationshipType relationshipType = new SimpleRelationshipType(reader.getValue());
        reader.moveUp(); // type

        reader.moveDown(); // startNode
        long startNodeId = Long.parseLong(reader.getValue());
        reader.moveUp(); // startNode

        reader.moveDown(); // endNode
        long endNodeId = Long.parseLong(reader.getValue());
        reader.moveUp(); // endNode

        Relationship relationship;

        Transaction tx = neo.beginTx();
        try {

            try {
                relationship = neo.getRelationshipById(Long.parseLong(id));
            } catch (NotFoundException exception) {
                Node startNode = neo.getNodeById(nodeIdMap == null ? startNodeId : nodeIdMap.get(startNodeId));
                Node endNode = neo.getNodeById(nodeIdMap == null ? endNodeId : nodeIdMap.get(endNodeId));
                relationship = startNode.createRelationshipTo(endNode, relationshipType);
            } catch (NumberFormatException exception) {
                Node startNode = neo.getNodeById(nodeIdMap == null ? startNodeId : nodeIdMap.get(startNodeId));
                Node endNode = neo.getNodeById(nodeIdMap == null ? endNodeId : nodeIdMap.get(endNodeId));
                relationship = startNode.createRelationshipTo(endNode, relationshipType);
            }

            Set<String> keys = NeoUtils.getKeysSet(relationship);

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

                    relationship.setProperty(key, value);
                    keys.remove(key);

                }

                reader.moveUp(); // properties

            }

            for (String key : keys) {
                relationship.removeProperty(key);
            }

            tx.success();
        } finally {
            tx.finish();
        }

        return relationship;

    }

}

