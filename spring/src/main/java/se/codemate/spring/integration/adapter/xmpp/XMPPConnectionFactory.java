package se.codemate.spring.integration.adapter.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.HashMap;
import java.util.Map;

public class XMPPConnectionFactory {

    private static final Map<String, XMPPConnection> connections = new HashMap<String, XMPPConnection>();
    private static final Map<XMPPConnection, Integer> counts = new HashMap<XMPPConnection, Integer>();

    private static String buildConnectionId(String server, int port, String username, String resource) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(username);
        stringBuilder.append("@");
        stringBuilder.append(server);
        if (port != 5222) {
            stringBuilder.append(":");
            stringBuilder.append(port);
        }
        if (resource != null) {
            stringBuilder.append("/");
            stringBuilder.append(resource);
        }
        return stringBuilder.toString();
    }

    public static XMPPConnection getConnection(String server, int port, String username, String password, String resource) throws XMPPException {
        XMPPConnection connection;
        synchronized (connections) {
            String connectionId = buildConnectionId(server, port, username, resource);
            if (connections.containsKey(connectionId)) {
                connection = connections.get(connectionId);
            } else {
                ConnectionConfiguration config = new ConnectionConfiguration(server, port);
                config.setCompressionEnabled(true);
                config.setSASLAuthenticationEnabled(true);
                connection = new XMPPConnection(config);
                connection.connect();
                connection.login(username, password, resource);
                connections.put(connectionId, connection);
            }
            counts.put(connection, counts.get(connection) == null ? 1 : counts.get(connection) + 1);
        }
        return connection;
    }

    public static void releaseConnection(XMPPConnection connection) {
        synchronized (connections) {
            if (counts.containsKey(connection)) {
                int count = Math.max(0, counts.get(connection) - 1);
                if (count == 0) {
                    String connectionId = null;
                    for (Map.Entry<String, XMPPConnection> entry : connections.entrySet()) {
                        if (entry.getValue() == connection) {
                            connectionId = entry.getKey();
                            break;
                        }
                    }
                    connections.remove(connectionId);
                    counts.remove(connection);
                    connection.disconnect();
                } else {
                    counts.put(connection, count);
                }
            }
        }
    }

}
