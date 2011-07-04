package se.codemate.spring.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import se.codemate.utils.UUIDGenerator;

@Aspect
public class NeoUUIDAspect {

    private static Logger log = Logger.getLogger(NeoUUIDAspect.class);

    @AfterReturning(value = "execution(* org.neo4j.graphdb.GraphDatabaseService.createNode())", returning = "node")
    public void createNode(Node node) {
        if (log.isDebugEnabled()) {
            log.debug("Node created, adding UUID to " + node);
        }
        node.setProperty("UUID", UUIDGenerator.generateUUID().toString());
    }

    @AfterReturning(value = "execution(* org.neo4j.graphdb.Node.createRelationshipTo(..))", returning = "relationship")
    public void createRelationship(Relationship relationship) {
        if (log.isDebugEnabled()) {
            log.debug("Relationship created, adding UUID to " + relationship);
        }
        relationship.setProperty("UUID", UUIDGenerator.generateUUID().toString());
    }

    @Around(value = "execution(* org.neo4j.graphdb.PropertyContainer.removeProperty(..))")
    public Object removeProperty(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args != null && args.length == 1 && "UUID".equals(args[0])) {
            if (log.isDebugEnabled()) {
                log.debug("Blocking deletion of UUID");
            }
            return null;
        } else {
            return pjp.proceed();
        }
    }

}
