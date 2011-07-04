package se.codemate.spring.integration.adapter.cometd.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.w3c.dom.Element;
import se.codemate.spring.integration.adapter.cometd.CometdSendingMessageHandler;

public class CometdOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {

    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CometdSendingMessageHandler.class);
        builder.addPropertyValue("cometdChannel", element.getAttribute("cometd-channel"));
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "bayeux");
        return builder.getBeanDefinition();
    }

}
