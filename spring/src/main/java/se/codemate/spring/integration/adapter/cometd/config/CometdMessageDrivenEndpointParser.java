package se.codemate.spring.integration.adapter.cometd.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

public class CometdMessageDrivenEndpointParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return CometdMessageDrivenEndpointParser.class;
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
        builder.addPropertyValue("cometdChannel", element.getAttribute("cometd-channel"));
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "bayeux");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "channel", "outputChannel");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "timeout", "sendTimeout");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "auto-startup", "autoStartup");
    }

}
