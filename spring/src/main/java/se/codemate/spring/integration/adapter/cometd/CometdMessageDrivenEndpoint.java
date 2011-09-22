package se.codemate.spring.integration.adapter.cometd;

import org.cometd.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.message.MessageBuilder;

public class CometdMessageDrivenEndpoint extends MessageProducerSupport implements MessageListener, MessageListener.Asynchronous {

    private Bayeux bayeux;
    private String cometdChannelId;

    private Client client;

    @Required
    public void setBayeux(Bayeux bayeux) {
        this.bayeux = bayeux;
    }

    @Required
    public void setCometdChannel(String cometdChannel) {
        this.cometdChannelId = cometdChannel;
    }

    protected void doStart() {
        client = bayeux.newClient(getBeanName());
        bayeux.getChannel(cometdChannelId, true).subscribe(client);
        client.addListener(this);
    }

    protected void doStop() {
        client.removeListener(this);
        Channel channel = bayeux.getChannel(cometdChannelId, false);
        if (channel != null) {
            channel.unsubscribe(client);
        }
        bayeux.removeClient(getBeanName());
        client = null;
    }

    public void deliver(Client fromClient, Client toClient, Message msg) {
        if (fromClient != client) {
            MessageBuilder messageBuilder = MessageBuilder.withPayload(msg)
                    .setHeader("cometd.fromClient.id", fromClient.getId())
                    .setHeader("cometd.toClient.id", toClient.getId())
                    .setHeader("cometd.message.id", msg.getId());
            sendMessage(messageBuilder.build());
        }
    }

}
