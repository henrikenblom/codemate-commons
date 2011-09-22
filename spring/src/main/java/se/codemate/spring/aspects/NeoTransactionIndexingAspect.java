package se.codemate.spring.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import se.codemate.neo4j.NeoSearch;

import java.io.IOException;
import java.util.*;

@Configurable
@Aspect
public class NeoTransactionIndexingAspect {

    private static Logger log = Logger.getLogger(NeoTransactionIndexingAspect.class);

    @Autowired
    private NeoSearch neoSearch;

    private Set<Node> nodes = new TreeSet<Node>(new Comparator<Node>() {
        public int compare(Node n1, Node n2) {
            return (int) (n1.getId() - n2.getId());
        }
    });

    private Set<Relationship> relationships = new TreeSet<Relationship>(new Comparator<Relationship>() {
        public int compare(Relationship r1, Relationship r2) {
            return (int) (r1.getId() - r2.getId());
        }
    });

    private void doIndex() {

        List<Node> nodeList = new LinkedList<Node>(nodes);
        nodes.clear();

        List<Relationship> relationshipList = new LinkedList<Relationship>(relationships);
        relationships.clear();

        if (log.isDebugEnabled()) {
            log.debug("Indexing " + nodeList.size() + " nodes and " + relationshipList.size() + " relationships");
        }

        for (Node node : nodeList) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Indexing " + node);
                }
                neoSearch.indexNode(node);
            } catch (IOException exception) {
                log.error("Error while indexing node", exception);
            }
        }


        for (Relationship relationship : relationshipList) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Indexing " + relationship);
                }
                neoSearch.indexRelationship(relationship);
            } catch (IOException exception) {
                log.error("Error while indexing relationship", exception);
            }
        }

    }


    @Before(value = "execution(* org.neo4j.graphdb.Transaction.finish()) && target(transaction)", argNames = "transaction")
    public void finishTx(Transaction transaction) {
        if (!relationships.isEmpty() || !nodes.isEmpty()) {
            doIndex();
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.GraphDatabaseService.createNode())", returning = "node")
    public void createNode(Node node) {
        if (nodes.add(node) && log.isDebugEnabled()) {
            log.debug("Node created, added " + node + " to list with " + nodes.size() + " entries");
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.delete()) && target(node)", argNames = "node")
    public void deleteNode(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node deleted, removing " + node + " from index");
        }
        try {
            neoSearch.removeNode(node);
            nodes.remove(node);
        } catch (IOException exception) {
            log.error("Error while removing node", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.setProperty(..)) && target(node)", argNames = "node")
    public void nodeSetProperty(Node node) {
        if (nodes.add(node) && log.isDebugEnabled()) {
            log.debug("Node modified, added " + node + " to list with " + nodes.size() + " entries");
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.removeProperty(..)) && target(node)", argNames = "node")
    public void nodeRemoveProperty(Node node) {
        if (nodes.add(node) && log.isDebugEnabled()) {
            log.debug("Node modified, added " + node + " to list with " + nodes.size() + " entries");
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.createRelationshipTo(..))", returning = "relationship")
    public void createRelationship(Relationship relationship) {
        if (relationships.add(relationship) && log.isDebugEnabled()) {
            log.debug("Relationship created, added " + relationship + " to list with " + relationships.size() + " entries");
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Relationship.delete()) && target(relationship)", argNames = "relationship")
    public void deleteRelationship(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship deleted, removing " + relationship + " from index");
        }
        try {
            neoSearch.removeRelationship(relationship);
            relationships.remove(relationship);
        } catch (IOException exception) {
            log.error("Error while removing relationship", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Relationship.setProperty(..)) && target(relationship)", argNames = "relationship")
    public void relationshipSetProperty(Relationship relationship) {
        if (relationships.add(relationship) && log.isDebugEnabled()) {
            log.debug("Relationship modified, added " + relationship + " to list with " + relationships.size() + " entries");
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Relationship.removeProperty(..)) && target(relationship)", argNames = "relationship")
    public void relationshipRemoveProperty(Relationship relationship) {
        if (relationships.add(relationship) && log.isDebugEnabled()) {
            log.debug("Relationship modified, added " + relationship + " to list with " + relationships.size() + " entries");
        }
    }

}
