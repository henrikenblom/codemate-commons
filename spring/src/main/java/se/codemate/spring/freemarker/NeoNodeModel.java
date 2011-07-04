package se.codemate.spring.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import se.codemate.neo4j.NeoUtils;
import se.codemate.neo4j.SimpleRelationshipType;

import java.util.List;
import java.util.Map;

public class NeoNodeModel extends StringModel implements TemplateMethodModelEx {

    static final ModelFactory FACTORY = new ModelFactory() {
        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            return new NeoNodeModel((Node) object, (BeansWrapper) wrapper);
        }
    };

    public NeoNodeModel(Node node, BeansWrapper wrapper) {
        super(node, wrapper);
    }

    @Override
    protected TemplateModel invokeGenericGet(Map keyMap, Class clazz, String key) throws TemplateModelException {
        Node node = (Node) object;
        if (node.hasProperty(key)) {
            return wrap(node.getProperty(key));
        } else {
            String type = key.trim();
            Direction direction = Direction.OUTGOING;
            if (type.endsWith("_BOTH")) {
                direction = Direction.BOTH;
                type = type.substring(0, type.length() - 5);
            } else if (type.endsWith("_OUTGOING")) {
                direction = Direction.OUTGOING;
                type = type.substring(0, type.length() - 9);

            } else if (type.endsWith("_INCOMING")) {
                direction = Direction.INCOMING;
                type = type.substring(0, type.length() - 9);
            }
            if (type.length() > 0) {
                SimpleRelationshipType relationshipType = new SimpleRelationshipType(type);
                if (node.hasRelationship(relationshipType, direction)) {
                    return wrap(NeoUtils.toRelationshipList(node.getRelationships(relationshipType, direction)));
                }
            } else {
                if (node.hasRelationship(direction)) {
                    return wrap(NeoUtils.toRelationshipList(node.getRelationships(direction)));
                }
            }
            return null;
        }
    }

    public Object exec(List arguments) throws TemplateModelException {
        String type = arguments.get(0).toString();
        Direction direction = Direction.OUTGOING;
        if (arguments.size() > 1) {
            if ("BOTH".equalsIgnoreCase(arguments.get(1).toString())) {
                direction = Direction.BOTH;
            } else if ("OUTGOING".equalsIgnoreCase(arguments.get(1).toString())) {
                direction = Direction.OUTGOING;
            } else if ("INCOMING".equalsIgnoreCase(arguments.get(1).toString())) {
                direction = Direction.INCOMING;
            }
        }
        Node node = (Node) object;
        SimpleRelationshipType relationshipType = new SimpleRelationshipType(type);
        if (node.hasRelationship(relationshipType, direction)) {
            return NeoUtils.toRelationshipList(node.getRelationships(relationshipType, direction));
        } else {
            throw new TemplateModelException("No such relationship: " + arguments);
        }
    }

}
