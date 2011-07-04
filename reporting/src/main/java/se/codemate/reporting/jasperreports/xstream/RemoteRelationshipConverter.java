package se.codemate.reporting.jasperreports.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

public class RemoteRelationshipConverter implements Converter {

    private Mapper mapper;

    public RemoteRelationshipConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // no-op
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("_type", "relationship");

        map.put("_id", Long.parseLong(reader.getAttribute("id")));

        reader.moveDown(); // type
        map.put("_relationshipType", reader.getValue());
        reader.moveUp(); // type

        reader.moveDown(); // startNode
        map.put("_startNodeId", Long.parseLong(reader.getValue()));
        reader.moveUp(); // startNode

        reader.moveDown(); // endNode
        map.put("_endNodeId", Long.parseLong(reader.getValue()));
        reader.moveUp(); // endNode

        if (reader.hasMoreChildren()) {

            reader.moveDown(); // properties

            while (reader.hasMoreChildren()) {

                reader.moveDown(); // property

                String key = reader.getAttribute("key");
                Class type = mapper.realClass(reader.getAttribute("class"));
                Object value = context.convertAnother(context.currentObject(), type);

                map.put(key, value);

                reader.moveUp(); // property

            }

            reader.moveUp(); // properties

        }

        return map;

    }

    public boolean canConvert(Class type) {
        return Relationship.class.isAssignableFrom(type);
    }

}
