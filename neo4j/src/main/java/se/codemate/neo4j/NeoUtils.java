package se.codemate.neo4j;

import org.neo4j.graphdb.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NeoUtils {

    private GraphDatabaseService neo;

    public NeoUtils(GraphDatabaseService neo) {
        this.neo = neo;
    }

    public static List<Node> toNodeList(Iterable<Node> nodes) {
        if (nodes == null) {
            return null;
        }
        ArrayList<Node> list = new ArrayList<Node>();
        for (Node node : nodes) {
            list.add(node);
        }
        return list;
    }

    public static List<Relationship> toRelationshipList(Iterable<Relationship> relationships) {
        if (relationships == null) {
            return null;
        }
        ArrayList<Relationship> list = new ArrayList<Relationship>();
        for (Relationship relationship : relationships) {
            list.add(relationship);
        }
        return list;
    }

    public static List<Object> toValueList(List propertyContainers, String key) {
        List<Object> valueList = new ArrayList<Object>();
        for (Object object : propertyContainers) {
            PropertyContainer propertyContainer = (PropertyContainer) object;
            valueList.add(propertyContainer.getProperty(key, null));
        }
        return valueList;
    }

    public static Set<String> getKeysSet(PropertyContainer propertyContainer) {
        Set<String> keys = new TreeSet<String>();
        for (String key : propertyContainer.getPropertyKeys()) {
            keys.add(key);
        }
        return keys;
    }

    public static Set<Object> getValuesSet(PropertyContainer propertyContainer) {
        Set<Object> values = new HashSet<Object>();
        for (String key : propertyContainer.getPropertyKeys()) {
            Object value = propertyContainer.getProperty(key,null);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    public static Object toPrimitive(String s, String type, String pattern) throws IllegalArgumentException {
        if (type != null) {
            if ("byte".equalsIgnoreCase(type)) {
                return Byte.parseByte(s);
            } else if ("short".equalsIgnoreCase(type)) {
                return Short.parseShort(s);
            } else if ("int".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type)) {
                return Integer.parseInt(s);
            } else if ("long".equalsIgnoreCase(type)) {
                return Long.parseLong(s);
            } else if ("float".equalsIgnoreCase(type)) {
                return Float.parseFloat(s);
            } else if ("double".equalsIgnoreCase(type)) {
                return Double.parseDouble(s);
            } else if ("boolean".equalsIgnoreCase(type)) {
                return Boolean.parseBoolean(s);
            } else if ("char".equalsIgnoreCase(type)) {
                return s.charAt(0);
            } else if ("date".equalsIgnoreCase(type)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern == null ? "yyyy-MM-dd" : pattern);
                try {
                    return dateFormat.parse(s);
                } catch (ParseException exception) {
                    throw new IllegalArgumentException("Bad date format", exception);
                }
            }
        }
        return s;
    }

    public Relationship getRelationship(Node node, String name, boolean create) {
        RelationshipType type = new SimpleRelationshipType(name);
        List<Relationship> relationships = toRelationshipList(node.getRelationships(type, Direction.OUTGOING));
        if (relationships == null || relationships.isEmpty()) {
            if (create) {
                return node.createRelationshipTo(neo.createNode(), type);
            } else {
                return null;
            }
        } else {
            return relationships.get(0);
        }
    }

    public void deleteRelationship(Relationship relationship) {
        Node[] nodes = relationship.getNodes();
        relationship.delete();
        for (Node connectedNode : nodes) {
            if (!hasPathToNode(connectedNode, neo.getReferenceNode())) {
                deleteNode(connectedNode);
            }
        }
    }

    public Set<Long> deleteNode(Node startNode) {

        LinkedList<Node> nodeQueue = new LinkedList<Node>();
        nodeQueue.add(startNode);

        Set<Long> deletedNodeIDs = new TreeSet<Long>();

        while (!nodeQueue.isEmpty()) {

            Node node = nodeQueue.removeFirst();

            if (!deletedNodeIDs.contains(node.getId())) {

                ArrayList<Node> connectedNodes = new ArrayList<Node>();
                for (Relationship relationship : node.getRelationships()) {
                    connectedNodes.add(relationship.getOtherNode(node));
                    relationship.delete();
                }

                deletedNodeIDs.add(node.getId());
                node.delete();

                for (Node connectedNode : connectedNodes) {
                    if (!hasPathToNode(connectedNode, neo.getReferenceNode())) {
                        nodeQueue.add(connectedNode);
                    }
                }

            }

        }

        return deletedNodeIDs;

    }

    public boolean hasPathToNode(Node startNode, Node otherNode) {
        return hasPathToNode(startNode, otherNode, new Stack<Node>());
    }

    private boolean hasPathToNode(Node startNode, Node endNode, Stack<Node> path) {
        try {
            path.push(startNode);
            for (Relationship relationship : startNode.getRelationships()) {
                Node otherNode = relationship.getOtherNode(startNode);
                if (otherNode.getId() == endNode.getId() || (!path.contains(otherNode) && hasPathToNode(otherNode, endNode, path))) {
                    return true;
                }
            }
            return false;
        } finally {
            path.pop();
        }
    }

}