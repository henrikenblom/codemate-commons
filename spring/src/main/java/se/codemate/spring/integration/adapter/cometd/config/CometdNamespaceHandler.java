package se.codemate.spring.integration.adapter.cometd.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CometdNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("sending-message-handler", new CometdSendingMessageHandlerParser());
        registerBeanDefinitionParser("message-driven-channel-adapter", new CometdMessageDrivenEndpointParser());
        registerBeanDefinitionParser("outbound-channel-adapter", new CometdOutboundChannelAdapterParser());
    }

}
