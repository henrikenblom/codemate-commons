package se.codemate.spring.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import se.codemate.neo4j.NeoSearch;

import java.io.IOException;

@Configurable
@Aspect
public class NeoIndexingAspect {

    private static Logger log = Logger.getLogger(NeoIndexingAspect.class);

    @Autowired
    private NeoSearch neoSearch;

    @AfterReturning(value = "execution(* org.neo4j.graphdb.GraphDatabaseService.createNode())", returning = "node")
    public void createNode(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node created, indexing " + node);
        }
        try {
            neoSearch.indexNode(node);
        } catch (IOException exception) {
            log.error("Error while indexing node", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.delete()) && target(node)", argNames = "node")
    public void deleteNode(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node deleted, removing " + node + " from index");
        }
        try {
            neoSearch.removeNode(node);
        } catch (IOException exception) {
            log.error("Error while removing node", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.setProperty(..)) && target(node)", argNames = "node")
    public void nodeSetProperty(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node property set, indexing " + node);
        }
        try {
            neoSearch.indexNode(node);
        } catch (IOException exception) {
            log.error("Error while indexing node", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.removeProperty(..)) && target(node)", argNames = "node")
    public void nodeRemoveProperty(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node property removed, indexing " + node);
        }
        try {
            neoSearch.indexNode(node);
        } catch (IOException exception) {
            log.error("Error while indexing node", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.createRelationshipTo(..))", returning = "relationship")
    public void createRelationship(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship created, indexing " + relationship);
        }
        try {
            neoSearch.indexRelationship(relationship);
        } catch (IOException exception) {
            log.error("Error while indexing relationship", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Relationship.delete()) && target(relationship)", argNames = "relationship")
    public void deleteRelationship(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship deleted, removing " + relationship + " from index");
        }
        try {
            neoSearch.removeRelationship(relationship);
        } catch (IOException exception) {
            log.error("Error while removing relationship", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Relationship.setProperty(..)) && target(relationship)", argNames = "relationship")
    public void relationshipSetProperty(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship property set, indexing " + relationship);
        }
        try {
            neoSearch.indexRelationship(relationship);
        } catch (IOException exception) {
            log.error("Error while indexing relationship", exception);
        }
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Relationship.removeProperty(..)) && target(relationship)", argNames = "relationship")
    public void relationshipRemoveProperty(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship property removed, indexing " + relationship);
        }
        try {
            neoSearch.indexRelationship(relationship);
        } catch (IOException exception) {
            log.error("Error while indexing relationship", exception);
        }
    }

}
