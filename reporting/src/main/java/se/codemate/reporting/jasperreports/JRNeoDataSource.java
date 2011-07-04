package se.codemate.reporting.jasperreports;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;
import java.util.List;

import se.codemate.neo4j.MapPropertyContainer;

public class JRNeoDataSource implements JRRewindableDataSource {

    private List<PropertyContainer> propertyContainers;

    private Iterator<PropertyContainer> iterator;

    private PropertyContainer currentPropertyContainer;

    public JRNeoDataSource(List<PropertyContainer> propertyContainers) throws JRException {
        this.propertyContainers = propertyContainers;
        moveFirst();
    }

    public boolean next() throws JRException {
        if (iterator.hasNext()) {
            currentPropertyContainer = iterator.next();
            return true;
        } else {
            currentPropertyContainer = null;
            return false;
        }
    }

    public Object getFieldValue(JRField jrField) throws JRException {
        if (currentPropertyContainer != null) {
            String fieldName = jrField.getName();
            if ("_type".equals(fieldName)) {
                if (Node.class.isInstance(currentPropertyContainer)) {
                    return "node";
                } else if (Relationship.class.isInstance(currentPropertyContainer)) {
                    return "relationship";
                } else if (MapPropertyContainer.class.isInstance(currentPropertyContainer)) {
                    return "map";
                } else {
                    return "unknown";
                }
            } else if ("_id".equals(fieldName)) {
                if (Node.class.isInstance(currentPropertyContainer)) {
                    return ((Node) currentPropertyContainer).getId();
                } else if (Relationship.class.isInstance(currentPropertyContainer)) {
                    return ((Relationship) currentPropertyContainer).getId();
                } else if (MapPropertyContainer.class.isInstance(currentPropertyContainer)) {
                    return ((MapPropertyContainer) currentPropertyContainer).getId();
                } else {
                    return -1;
                }
            } else {
                return currentPropertyContainer.getProperty(fieldName, null);
            }
        } else {
            return null;
        }
    }

    public void moveFirst() throws JRException {
        iterator = propertyContainers.iterator();
    }

}
