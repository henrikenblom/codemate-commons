package se.codemate.spring.sitemesh;

import com.opensymphony.module.sitemesh.freemarker.FreemarkerDecoratorServlet;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class SpringFreemarkerDecoratorServlet extends FreemarkerDecoratorServlet {

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {

        super.init();

        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

        Map<String, FreeMarkerConfigurer> freeMarkerConfigurerBeans = (Map<String, FreeMarkerConfigurer>) ctx.getBeansOfType(FreeMarkerConfigurer.class);

        if (freeMarkerConfigurerBeans == null || freeMarkerConfigurerBeans.size() != 1) {
            throw new ServletException("Unable to find a unique Spring bean of the type FreeMarkerConfigurer.");
        }

        Iterator<FreeMarkerConfigurer> iterator = freeMarkerConfigurerBeans.values().iterator();

        if (iterator.hasNext()) {
            FreeMarkerConfigurer freeMarkerConfigurer = iterator.next();
            Configuration freeMarkerConfiguration = freeMarkerConfigurer.getConfiguration();
            TemplateLoader templateLoader = freeMarkerConfiguration.getTemplateLoader();
            getConfiguration().setDefaultEncoding(freeMarkerConfiguration.getDefaultEncoding());
            getConfiguration().setTagSyntax(freeMarkerConfiguration.getTagSyntax());
            getConfiguration().setTemplateLoader(templateLoader);
        }

    }

    @Override
    protected boolean preTemplateProcess(HttpServletRequest request, HttpServletResponse response, Template template, TemplateModel templateModel) throws ServletException, IOException {
        boolean result = super.preTemplateProcess(request, response, template, templateModel);
        SimpleHash hash = (SimpleHash) templateModel;
        hash.put("locale", RequestContextUtils.getLocale(request));
        hash.put("theme", RequestContextUtils.getTheme(request).getName());
        return result;
    }

}
