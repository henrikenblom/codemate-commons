import org.neo4j.graphdb.*
import se.codemate.neo4j.*

Node root = neo.getReferenceNode();

List<PropertyContainer> results = new LinkedList<PropertyContainer>();

def returnEvaluator = {
  if (it.depth() == 3) {
    String salary = it.currentNode().getProperty("emp_salary", null);
    if (salary != null) {
      try {
        MapPropertyContainer propertyContainer = new MapPropertyContainer();
        propertyContainer.setProperty("gender", it.previousNode().getProperty("gender", "?"));
        propertyContainer.setProperty("salary", Integer.parseInt(salary.replaceAll("\\s+", "")));
        results.add(propertyContainer);
        return true;
      } catch (NumberFormatException exception) {
        return false;
      }
    }
  }
  return false;
} as ReturnableEvaluator

def stopEvaluator = {
  return it.depth() == 3;
} as StopEvaluator

root.traverse(
        Traverser.Order.DEPTH_FIRST,
        stopEvaluator,
        returnEvaluator,
        SimpleRelationshipType.withName("HAS_ORGANIZATION"),
        Direction.OUTGOING,
        SimpleRelationshipType.withName("HAS_EMPLOYEE"),
        Direction.OUTGOING,
        SimpleRelationshipType.withName("HAS_EMPLOYMENT"),
        Direction.OUTGOING
).getAllNodes();

return results;