package se.codemate.spring.freemarker;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;

public class AdvancedFreemarkerConfigurer extends FreeMarkerConfigurer {

    private ObjectWrapper objectWrapper;

    public void setObjectWrapper(ObjectWrapper objectWrapper) {
        this.objectWrapper = objectWrapper;
    }

    protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException {
        if (objectWrapper != null) {
            config.setObjectWrapper(objectWrapper);
        }
    }

}