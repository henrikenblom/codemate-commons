package se.codemate.spring.integration.adapter.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.message.MessageBuilder;

public class XMPPMessageDrivenEndpoint extends AbstractEndpoint implements DisposableBean, PacketListener {

    private String server;
    private int port = 5222;

    private String username;
    private String password;
    private String resource;

    private XMPPConnection connection;

    private volatile MessageChannel outputChannel;

    private MessageChannelTemplate channelTemplate = new MessageChannelTemplate();

    @Required
    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Required
    public void setUsername(String username) {
        this.username = username;
    }

    @Required
    public void setPassword(String password) {
        this.password = password;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Required
    public void setOutputChannel(MessageChannel outputChannel) {
        this.outputChannel = outputChannel;
    }

    public void setSendTimeout(long sendTimeout) {
        channelTemplate.setSendTimeout(sendTimeout);
    }

    @Override
    protected void onInit() throws Exception {
        connection = XMPPConnectionFactory.getConnection(server, port, username, password, resource);
    }

    protected void doStart() {
        connection.addPacketListener(this, new PacketTypeFilter(Message.class));
    }

    protected void doStop() {
        connection.removePacketListener(this);
    }

    public void destroy() throws Exception {
        if (isRunning()) {
            stop();
        }
        XMPPConnectionFactory.releaseConnection(connection);
    }

    public void processPacket(Packet packet) {
        final Message message = (Message) packet;
        final String body = message.getBody();
        if (body != null) {
            MessageBuilder messageBuilder = MessageBuilder.withPayload(body);
            if (packet.getTo() != null) {
                messageBuilder.setHeader("to", packet.getTo());
            }
            if (packet.getFrom() != null) {
                messageBuilder.setHeader("from", packet.getFrom());
            }
            channelTemplate.send(messageBuilder.build(), outputChannel);
        }
    }

}
