package se.codemate.spring.integration.adapter.xmpp.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class XMPPNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("receiving-message-source", new XMPPReceivingMessageSourceParser());
        registerBeanDefinitionParser("sending-message-handler", new XMPPSendingMessageHandlerParser());
        registerBeanDefinitionParser("message-driven-channel-adapter", new XMPPMessageDrivenEndpointParser());
        registerBeanDefinitionParser("inbound-channel-adapter", new XMPPInboundChannelAdapterParser());
        registerBeanDefinitionParser("outbound-channel-adapter", new XMPPOutboundChannelAdapterParser());
    }

}
