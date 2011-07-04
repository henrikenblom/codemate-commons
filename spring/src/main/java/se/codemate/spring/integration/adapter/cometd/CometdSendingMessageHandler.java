package se.codemate.spring.integration.adapter.cometd;

import org.cometd.Bayeux;
import org.cometd.Channel;
import org.cometd.Client;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.integration.core.Message;
import org.springframework.integration.handler.AbstractMessageHandler;

public class CometdSendingMessageHandler extends AbstractMessageHandler implements BeanNameAware, InitializingBean, DisposableBean {

    private Bayeux bayeux;
    private String channelId;

    private Client client;
    private Channel channel;

    private String name;

    @Required
    public void setBayeux(Bayeux bayeux) {
        this.bayeux = bayeux;
    }

    @Required
    public void setCometdChannel(String cometdChannel) {
        this.channelId = cometdChannel;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public String getBeanName() {
        return name;
    }

    public void afterPropertiesSet() throws Exception {
        client = bayeux.newClient(getBeanName());
        channel = bayeux.getChannel(channelId, true);
        channel.subscribe(client);
    }

    public void destroy() throws Exception {
        if (channel != null) {
            channel.unsubscribe(client);
            channel = null;
        }
        bayeux.removeClient(getBeanName());
        client = null;
    }

    protected void handleMessageInternal(Message<?> message) throws Exception {
        channel.publish(client, message.getPayload(), null);
    }

}
