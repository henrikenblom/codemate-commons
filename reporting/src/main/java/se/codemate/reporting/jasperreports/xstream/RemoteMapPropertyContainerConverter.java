package se.codemate.reporting.jasperreports.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import se.codemate.neo4j.MapPropertyContainer;

import java.util.HashMap;
import java.util.Map;

public class RemoteMapPropertyContainerConverter implements Converter {

    private Mapper mapper;

    public RemoteMapPropertyContainerConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        // no-op
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("_type", "map");

        map.put("_id", Long.parseLong(reader.getAttribute("id")));

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
        return MapPropertyContainer.class.isAssignableFrom(type);
    }

}
