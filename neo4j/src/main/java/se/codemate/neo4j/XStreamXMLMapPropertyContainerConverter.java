package se.codemate.neo4j;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.neo4j.graphdb.Node;

public class XStreamXMLMapPropertyContainerConverter implements Converter {

    private Mapper mapper;

    public XStreamXMLMapPropertyContainerConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public boolean canConvert(Class type) {
        return MapPropertyContainer.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshal(source, (ExtendedHierarchicalStreamWriter) writer, context);
    }

    public void marshal(Object source, ExtendedHierarchicalStreamWriter writer, MarshallingContext context) {

        MapPropertyContainer mapPropertyContainer = (MapPropertyContainer) source;

        writer.addAttribute("id", Long.toString(mapPropertyContainer.getId()));

        boolean hasProperties = false;
        for (String key : mapPropertyContainer.getPropertyKeys()) {
            if (!hasProperties) {
                writer.startNode("properties");
                hasProperties = true;
            }
            Object value = mapPropertyContainer.getProperty(key);
            writer.startNode("property", value.getClass());
            writer.addAttribute("key", key);
            writer.addAttribute("class", mapper.serializedClass(value.getClass()));
            context.convertAnother(value);
            writer.endNode(); // property

        }
        if (hasProperties) {
            writer.endNode(); // properties
        }

    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        MapPropertyContainer mapPropertyContainer = new MapPropertyContainer();

        if (reader.hasMoreChildren()) {

            reader.moveDown(); // properties

            while (reader.hasMoreChildren()) {

                reader.moveDown(); // property

                String key = reader.getAttribute("key");
                Class type = mapper.realClass(reader.getAttribute("class"));
                Object value = context.convertAnother(context.currentObject(), type);

                reader.moveUp(); // property

                mapPropertyContainer.setProperty(key, value);

            }

            reader.moveUp(); // properties

        }

        return mapPropertyContainer;

    }

}