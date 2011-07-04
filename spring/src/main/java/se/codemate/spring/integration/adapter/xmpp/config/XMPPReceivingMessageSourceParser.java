package se.codemate.spring.integration.adapter.xmpp.config;

import se.codemate.spring.integration.adapter.xmpp.XMPPReceivingMessageSource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;

public class XMPPReceivingMessageSourceParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return XMPPReceivingMessageSource.class;
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
    }

}
