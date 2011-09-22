package se.codemate.spring.integration.adapter.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.message.MessageSource;

import java.util.LinkedList;

public class XMPPReceivingMessageSource extends XMPPAdapter implements MessageSource<String>, PacketListener {

    private final LinkedList<Message<String>> inboundQueue = new LinkedList<Message<String>>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        getConnection().addPacketListener(this, new PacketTypeFilter(org.jivesoftware.smack.packet.Message.class));
    }

    @Override
    public void destroy() throws Exception {
        getConnection().removePacketListener(this);
        super.destroy();
    }

    @SuppressWarnings("unchecked")
    public void processPacket(Packet packet) {
        final org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
        final String body = message.getBody();
        if (body != null) {
            MessageBuilder messageBuilder = MessageBuilder.withPayload(body);
            if (packet.getTo() != null) {
                messageBuilder.setHeader("to", packet.getTo());
            }
            if (packet.getFrom() != null) {
                messageBuilder.setHeader("from", packet.getFrom());
            }
            synchronized (inboundQueue) {
                inboundQueue.addLast(messageBuilder.build());
            }
        }
    }

    public Message<String> receive() {
        synchronized (inboundQueue) {
            return inboundQueue.isEmpty() ? null : inboundQueue.removeFirst();
        }
    }

}
