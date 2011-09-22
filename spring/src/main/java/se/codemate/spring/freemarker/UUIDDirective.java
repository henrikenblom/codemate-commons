package se.codemate.spring.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import se.codemate.utils.UUIDGenerator;

import java.io.IOException;
import java.util.Map;

public class UUIDDirective implements TemplateDirectiveModel {
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        env.getOut().write(UUIDGenerator.generateUUID().toString());
    }
}
