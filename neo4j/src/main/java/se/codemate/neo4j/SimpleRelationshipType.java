package se.codemate.neo4j;

import org.neo4j.graphdb.RelationshipType;

public class SimpleRelationshipType implements RelationshipType {

    private String name;

    public SimpleRelationshipType(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String toString() {
        return name();
    }

    public static SimpleRelationshipType withName(String name) {
        return new SimpleRelationshipType(name);
    }

}
