package se.codemate.spring.integration.adapter.xmpp;

import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;

public class XMPPSendingMessageHandler extends XMPPAdapter implements MessageHandler {

    public void handleMessage(Message<?> message) {

        final String body = message.getPayload().toString();
        final String to = message.getHeaders().get("to", String.class);

        final org.jivesoftware.smack.packet.Message packet = new org.jivesoftware.smack.packet.Message(to);
        packet.setBody(body);

        getConnection().sendPacket(packet);

    }

}
