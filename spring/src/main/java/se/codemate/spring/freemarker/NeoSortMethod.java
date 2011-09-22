package se.codemate.spring.freemarker;

import freemarker.template.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// ${neoSort(node.HAS_EMPLOYE![],"endNode.lastname:ASC","endNode.firstname:DESC")}

public class NeoSortMethod implements TemplateMethodModelEx {

    private class NeoSortComparator implements Comparator<PropertyContainer> {

        String name;
        boolean isAscending;
        NeoSortComparator nextComparator;

        private NeoSortComparator(String pattern, NeoSortComparator nextComparator) {
            String[] fields = pattern.split(":");
            this.name = fields[0];
            this.isAscending = fields.length != 2 || "asc".equalsIgnoreCase(fields[1]);
            this.nextComparator = nextComparator;
        }

        @SuppressWarnings("unchecked")
        public int compare(PropertyContainer pc1, PropertyContainer pc2) {

            Comparable v1;
            Comparable v2;

            if (Relationship.class.isInstance(pc1) && Relationship.class.isInstance(pc2)) {
                Relationship r1 = (Relationship) pc1;
                Relationship r2 = (Relationship) pc2;
                if (name.startsWith("endNode.")) {
                    String key = name.substring(8);
                    v1 = "id".equals(key) ? r1.getId() : (Comparable) r1.getEndNode().getProperty(key, null);
                    v2 = "id".equals(key) ? r2.getId() : (Comparable) r2.getEndNode().getProperty(key, null);
                } else if (name.startsWith("startNode.")) {
                    String key = name.substring(10);
                    v1 = "id".equals(key) ? r1.getId() : (Comparable) r1.getStartNode().getProperty(key, null);
                    v2 = "id".equals(key) ? r2.getId() : (Comparable) r2.getStartNode().getProperty(key, null);
                } else {
                    v1 = "id".equals(name) ? r1.getId() : (Comparable) r1.getProperty(name, null);
                    v2 = "id".equals(name) ? r2.getId() : (Comparable) r2.getProperty(name, null);
                }
            } else {
                if ("id".equals(name)) {
                    v1 = Relationship.class.isInstance(pc1) ? ((Relationship) pc1).getId() : ((Node) pc1).getId();
                    v2 = Relationship.class.isInstance(pc2) ? ((Relationship) pc2).getId() : ((Node) pc2).getId();
                } else {
                    v1 = (Comparable) pc1.getProperty(name, null);
                    v2 = (Comparable) pc2.getProperty(name, null);
                }
            }

            int comparison;

            if (v1 == null && v2 == null) {
                comparison = 0;
            } else if (v1 == null) {
                // v1 == null && v2 != null
                comparison = isAscending ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            } else if (v2 == null) {
                // v1 != null && v2 == null
                comparison = isAscending ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            } else {
                comparison = isAscending ? v1.compareTo(v2) : v2.compareTo(v1);
            }

            if (comparison == 0 && nextComparator != null) {
                return nextComparator.compare(pc1, pc2);
            } else {
                return comparison;
            }

        }

    }

    @SuppressWarnings("unchecked")
    public TemplateModel exec(List args) throws TemplateModelException {

        SimpleSequence sequence = null;
        ObjectWrapper objectWrapper = null;

        if (SimpleSequence.class.isInstance(args.get(0))) {
            sequence = (SimpleSequence) args.get(0);
            objectWrapper = sequence.getObjectWrapper();
        }

        if (SimpleCollection.class.isInstance(args.get(0))) {
            sequence = new SimpleSequence((SimpleCollection) args.get(0));
            objectWrapper = ((SimpleCollection) args.get(0)).getObjectWrapper();
        }

        if (sequence != null) {
            if (sequence.size() > 0) {
                NeoSortComparator comparator = null;
                for (int i = args.size() - 1; i > 0; i--) {
                    comparator = new NeoSortComparator(args.get(i).toString(), comparator);
                }
                List list = sequence.toList();
                Collections.sort(list, comparator);
                return new SimpleSequence(list, objectWrapper);
            } else {
                return sequence;
            }
        } else {
            throw new TemplateModelException("First argument must be a SimpleSequence or a SimpleCollection");
        }

    }
}
