package se.codemate.spring.aspects;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import se.codemate.neo4j.NeoSearch;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Configurable
@Aspect
public class UniquePropertyAspect {

    private static Logger log = Logger.getLogger(NeoIndexingAspect.class);

    @Autowired
    private NeoSearch neoSearch;

    @Autowired
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Before(value = "execution(* org.neo4j.graphdb.Node.setProperty(..)) && target(node) && args(key,value)", argNames = "node,key,value")
    public void nodeSetProperty(Node node, String key, Object value) throws IOException {
        try {
            Set<String> uniqueNameSet = (Set<String>) applicationContext.getBean("uniqueNodePropertyNames");
            if (uniqueNameSet.contains(key)) {
                List<Node> nodes = neoSearch.getNodes(new TermQuery(new Term(key, value.toString())));
                for (Node otherNode : nodes) {
                    if (!node.equals(otherNode)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Property '" + key + "' with value '" + value + "' exists in " + otherNode);
                        }
                        throw new UniquePropertyViolationException("Property '" + key + "' with value '" + value + "' exists in " + otherNode);
                    }
                }
            }
        } catch (NoSuchBeanDefinitionException exception) {
            // no-op
        }
    }

    @SuppressWarnings("unchecked")
    @Before(value = "execution(* org.neo4j.graphdb.Relationship.setProperty(..)) && target(relationship) && args(key,value)", argNames = "relationship,key,value")
    public void relationshipSetProperty(Relationship relationship, String key, Object value) throws IOException {
        try {
            Set<String> uniqueNameSet = (Set<String>) applicationContext.getBean("uniqueRelationshipPropertyNames");
            if (uniqueNameSet != null && uniqueNameSet.contains(key)) {
                List<Relationship> relationships = neoSearch.getRelationships(new TermQuery(new Term(key, value.toString())));
                for (Relationship otherRelationship : relationships) {
                    if (!relationship.equals(otherRelationship)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Property '" + key + "' with value '" + value + "' exists in " + otherRelationship);
                        }
                        throw new UniquePropertyViolationException("Property '" + key + "' with value '" + value + "' exists in " + otherRelationship);
                    }
                }
            }
        } catch (NoSuchBeanDefinitionException exception) {
            // no-op
        }
    }

}
