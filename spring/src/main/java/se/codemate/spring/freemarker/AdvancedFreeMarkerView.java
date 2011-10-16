package se.codemate.spring.freemarker;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class AdvancedFreeMarkerView extends FreeMarkerView {

    @Override
    protected void processTemplate(Template template, SimpleHash model, HttpServletResponse response) throws IOException, TemplateException {
        Object attrContentType = template.getCustomAttribute("content_type");
        if (attrContentType != null) {
            response.setContentType(attrContentType.toString());
        }
        super.processTemplate(template, model, response);
    }

}
