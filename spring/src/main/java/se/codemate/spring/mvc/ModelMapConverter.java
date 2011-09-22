package se.codemate.spring.mvc;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.springframework.ui.ModelMap;

public class ModelMapConverter implements Converter {

    private Mapper mapper;

    public ModelMapConverter(Mapper mapper) {
        this.mapper = mapper;
    }

    public boolean canConvert(Class type) {
        return type.equals(ModelMap.class);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        marshal(source, (ExtendedHierarchicalStreamWriter) writer, context);
    }

    public void marshal(Object source, ExtendedHierarchicalStreamWriter writer, MarshallingContext context) {
        ModelMap modelMap = (ModelMap) source;
        for (Object key : modelMap.keySet()) {
            Object value = modelMap.get(key);
            writer.startNode(key.toString());
            writer.startNode("class", String.class);
            writer.setValue(mapper.serializedClass(value.getClass()));
            writer.endNode(); // class
            writer.startNode("value", value.getClass());
            context.convertAnother(value);
            writer.endNode(); // value
            writer.endNode(); // key
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ModelMap modelMap = new ModelMap();
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
            modelMap.addAttribute(key, value);
        }
        return modelMap;
    }

}
