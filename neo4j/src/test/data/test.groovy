import org.neo4j.graphdb.*
import se.codemate.neo4j.*

Node boss = neo.getNodeById(0)

def returnEvaluator = {
  return it.notStartNode();
} as ReturnableEvaluator

def stopEvaluator = {
  return it.currentNode().getProperty("name") == "deleteD" || it.depth() >= 2;
} as StopEvaluator

Traverser traverser = boss.traverse(
  Traverser.Order.DEPTH_FIRST,
  stopEvaluator,
  returnEvaluator,
  SimpleRelationshipType.withName("CONNECTED_TO"),
  Direction.OUTGOING
)

return traverser.getAllNodes();