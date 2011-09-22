package se.codemate.spring.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

@Aspect
public class NeoTimestampAspect {

    private static Logger log = Logger.getLogger(NeoTimestampAspect.class);

    @AfterReturning(value = "execution(* org.neo4j.graphdb.GraphDatabaseService.createNode())", returning = "node")
    public void createNode(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node created, timestamping " + node);
        }
        node.setProperty("TS_CREATED", System.currentTimeMillis());
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.createRelationshipTo(..))", returning = "relationship")
    public void createRelationship(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship created, timestamping " + relationship);
        }
        relationship.setProperty("TS_CREATED", System.currentTimeMillis());
    }

    @Around(value = "execution(* org.neo4j.graphdb.PropertyContainer.setProperty(..))")
    public Object setProperty(ProceedingJoinPoint pjp) throws Throwable {
        String name = (String) pjp.getArgs()[0];
        Object returnValue = pjp.proceed();
        if (name != null && !name.startsWith("TS_") && !name.equals("UUID")) {
            PropertyContainer propertyContainer = (PropertyContainer) pjp.getTarget();
            if (log.isDebugEnabled()) {
                log.debug("Property '" + name + "' modified, timestamping " + propertyContainer);
            }
            propertyContainer.setProperty("TS_MODIFIED", System.currentTimeMillis());
        }
        return returnValue;
    }

    @Around(value = "execution(* org.neo4j.graphdb.PropertyContainer.removeProperty(..))")
    public Object removeProperty(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args != null && args.length == 1 && args[0].toString().startsWith("TS_")) {
            if (log.isDebugEnabled()) {
                log.debug("Blocking deletion of timestamp field");
            }
            return null;
        } else {
            PropertyContainer propertyContainer = (PropertyContainer) pjp.getTarget();
            try {
                return pjp.proceed();
            } finally {
                if (log.isDebugEnabled()) {
                    log.debug("Property removed, timestamping " + propertyContainer);
                }
                propertyContainer.setProperty("TS_MODIFIED", System.currentTimeMillis());
            }
        }
    }

}
