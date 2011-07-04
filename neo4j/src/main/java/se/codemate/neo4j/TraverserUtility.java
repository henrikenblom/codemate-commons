package se.codemate.neo4j;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

public class TraverserUtility {

    public static Map toMap(Traverser traverser) {

        Map map = new HashMap();

        for (Node node : traverser) {
            System.out.println(node);
            TraversalPosition position = traverser.currentPosition();
            Relationship relationship = position.lastRelationshipTraversed();
            System.out.println("  "+position.previousNode()+" -> "+position.currentNode()+" "+position.lastRelationshipTraversed()+" "+position.depth());
        }

        return map;

    }
}
