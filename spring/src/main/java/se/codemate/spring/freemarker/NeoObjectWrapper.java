package se.codemate.spring.freemarker;

import freemarker.ext.util.ModelFactory;
import freemarker.template.DefaultObjectWrapper;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import se.codemate.neo4j.SimpleRelationshipType;

public class NeoObjectWrapper extends DefaultObjectWrapper {

    @Override
    protected ModelFactory getModelFactory(Class clazz) {
        if (Node.class.isAssignableFrom(clazz)) {
            return NeoNodeModel.FACTORY;
        } else if (Relationship.class.isAssignableFrom(clazz)) {
            return NeoRelationshipModel.FACTORY;
        }
        return super.getModelFactory(clazz);
    }

}
