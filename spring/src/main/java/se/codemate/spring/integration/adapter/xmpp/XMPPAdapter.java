package se.codemate.spring.integration.adapter.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class XMPPAdapter implements InitializingBean, DisposableBean {

    private String server;
    private int port = 5222;

    private String username;
    private String password;
    private String resource;

    private XMPPConnection connection;

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

    public XMPPConnection getConnection() {
        return connection;
    }

    public void afterPropertiesSet() throws Exception {
        connection = XMPPConnectionFactory.getConnection(server, port, username, password, resource);
    }

    public void destroy() throws Exception {
        XMPPConnectionFactory.releaseConnection(connection);
    }

}
