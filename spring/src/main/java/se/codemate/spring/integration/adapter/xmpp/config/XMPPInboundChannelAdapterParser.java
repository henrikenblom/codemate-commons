package se.codemate.spring.integration.adapter.xmpp.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractPollingInboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;
import se.codemate.spring.integration.adapter.xmpp.XMPPReceivingMessageSource;

public class XMPPInboundChannelAdapterParser extends AbstractPollingInboundChannelAdapterParser {

    protected String parseSource(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XMPPReceivingMessageSource.class);
        builder.addPropertyValue("server", element.getAttribute("server"));
        builder.addPropertyValue("username", element.getAttribute("username"));
        builder.addPropertyValue("password", element.getAttribute("password"));
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "resource");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "port");
        return BeanDefinitionReaderUtils.registerWithGeneratedName(builder.getBeanDefinition(), parserContext.getRegistry());
    }

}
