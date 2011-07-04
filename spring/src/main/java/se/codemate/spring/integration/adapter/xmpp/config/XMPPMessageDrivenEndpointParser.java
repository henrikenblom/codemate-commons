package se.codemate.spring.integration.adapter.xmpp.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;
import se.codemate.spring.integration.adapter.xmpp.XMPPMessageDrivenEndpoint;

public class XMPPMessageDrivenEndpointParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return XMPPMessageDrivenEndpoint.class;
    }

    @Override
    protected boolean shouldGenerateId() {
        return false;
    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.addPropertyValue("server", element.getAttribute("server"));
        builder.addPropertyValue("username", element.getAttribute("username"));
        builder.addPropertyValue("password", element.getAttribute("password"));
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "resource");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "port");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "channel", "outputChannel");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "timeout", "sendTimeout");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "auto-startup", "autoStartup");
    }

}
