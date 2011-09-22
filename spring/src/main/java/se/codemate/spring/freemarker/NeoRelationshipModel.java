package se.codemate.spring.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

public class NeoRelationshipModel extends StringModel {

    static final ModelFactory FACTORY = new ModelFactory() {
        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            return new NeoRelationshipModel((Relationship) object, (BeansWrapper) wrapper);
        }
    };

    public NeoRelationshipModel(Relationship relationship, BeansWrapper wrapper) {
        super(relationship, wrapper);
    }

    @Override
    protected TemplateModel invokeGenericGet(Map keyMap, Class clazz, String key) throws TemplateModelException {
        Relationship relationship = (Relationship) object;
        if (relationship.hasProperty(key)) {
            return wrap(relationship.getProperty(key));
        } else {
            return null;
        }
    }

}
