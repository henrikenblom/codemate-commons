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

public class XStreamXMLRelationshipConverter implements Converter {

    private GraphDatabaseService neo;
    private Mapper mapper;

    public XStreamXMLRelationshipConverter(Mapper mapper) {
        this(null, mapper);
    }

    public XStreamXMLRelationshipConverter(GraphDatabaseService neo, Mapper mapper) {
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

        writer.addAttribute("id", Long.toString(relationship.getId()));

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
                writer.startNode("property", value.getClass());
                writer.addAttribute("key", key);
                writer.addAttribute("class", mapper.serializedClass(value.getClass()));
                context.convertAnother(value);
                writer.endNode(); // property

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

        String idstr = reader.getAttribute("id");

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
                long id = Long.parseLong(idstr);
                if (id < 0) {
                    Node startNode = neo.getNodeById(nodeIdMap == null ? startNodeId : (nodeIdMap.containsKey(startNodeId) ? nodeIdMap.get(startNodeId) : startNodeId));
                    Node endNode = neo.getNodeById(nodeIdMap == null ? endNodeId : (nodeIdMap.containsKey(endNodeId) ? nodeIdMap.get(endNodeId) : endNodeId));
                    relationship = startNode.createRelationshipTo(endNode, relationshipType);
                } else {
                    relationship = neo.getRelationshipById(id);
                }
            } catch (NotFoundException exception) {
                Node startNode = neo.getNodeById(nodeIdMap == null ? startNodeId : (nodeIdMap.containsKey(startNodeId) ? nodeIdMap.get(startNodeId) : startNodeId));
                Node endNode = neo.getNodeById(nodeIdMap == null ? endNodeId : (nodeIdMap.containsKey(endNodeId) ? nodeIdMap.get(endNodeId) : endNodeId));
                relationship = startNode.createRelationshipTo(endNode, relationshipType);
            } catch (NumberFormatException exception) {
                Node startNode = neo.getNodeById(nodeIdMap == null ? startNodeId : (nodeIdMap.containsKey(startNodeId) ? nodeIdMap.get(startNodeId) : startNodeId));
                Node endNode = neo.getNodeById(nodeIdMap == null ? endNodeId : (nodeIdMap.containsKey(endNodeId) ? nodeIdMap.get(endNodeId) : endNodeId));
                relationship = startNode.createRelationshipTo(endNode, relationshipType);
            }

            Set<String> keys = NeoUtils.getKeysSet(relationship);

            if (reader.hasMoreChildren()) {

                reader.moveDown(); // properties

                while (reader.hasMoreChildren()) {

                    reader.moveDown(); // property

                    String key = reader.getAttribute("key");
                    Class type = mapper.realClass(reader.getAttribute("class"));
                    Object value = context.convertAnother(context.currentObject(), type);

                    reader.moveUp(); // property

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

