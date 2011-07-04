package se.codemate.spring.mvc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AnnotationAndUrlHandlerMapping extends DefaultAnnotationHandlerMapping {

    private final Map<String, Object> urlMap = new HashMap<String, Object>();

    public void setMappings(Properties mappings) {
        for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
            this.urlMap.put(entry.getKey().toString(), entry.getValue());
        }
    }

    public void setUrlMap(Map<String, Object> urlMap) {
        this.urlMap.putAll(urlMap);
    }

    public Map<String, Object> getUrlMap() {
        return this.urlMap;
    }

    public void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        registerHandlers(this.urlMap);
    }

    protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
        if (urlMap.isEmpty()) {
            logger.warn("Neither 'urlMap' nor 'mappings' set on AnnotationAndUrlHandlerMapping");
        } else {
            for (String url : urlMap.keySet()) {
                Object handler = urlMap.get(url);
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                if (handler instanceof String) {
                    handler = ((String) handler).trim();
                }
                registerHandler(url, handler);
            }
        }
    }

}
