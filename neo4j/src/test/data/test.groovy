import org.neo4j.graphdb.*
import se.codemate.neo4j.*

Node boss = neo.getNodeById(97)

def returnEvaluator = {
  return  it.notStartNode() && it.currentNode().getProperty("nodeClass",null) == "employee";
} as ReturnableEvaluator

def stopEvaluator = {
  return it.currentNode().getProperty("emp_finish",null) != null || it.depth() >= 2;
} as StopEvaluator

Traverser traverser = boss.traverse(
  Traverser.Order.DEPTH_FIRST,
  stopEvaluator,
  returnEvaluator,
  SimpleRelationshipType.withName("REPORTS_TO"),
  Direction.INCOMING,
  SimpleRelationshipType.withName("HAS_EMPLOYMENT"),
  Direction.INCOMING
)

return traverser.getAllNodes();