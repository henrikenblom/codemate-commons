package se.codemate.spring.mvc;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.springframework.ui.ModelMap;

import java.util.Map;

public class SimpleMapConverter extends MapConverter {

    public SimpleMapConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class type) {
        return super.canConvert(type) || type.equals(ModelMap.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshal(source, (ExtendedHierarchicalStreamWriter) writer, context);
    }

    public void marshal(Object source, ExtendedHierarchicalStreamWriter writer, MarshallingContext context) {
        Map map = (Map) source;
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            writer.startNode(key.toString(), value.getClass());
            context.convertAnother(value);
            writer.endNode(); // key
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return super.unmarshal(reader, context);
    }

}
