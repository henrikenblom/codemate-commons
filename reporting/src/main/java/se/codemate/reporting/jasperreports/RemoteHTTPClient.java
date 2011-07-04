package se.codemate.reporting.jasperreports;

import com.thoughtworks.xstream.XStream;
import net.sf.jasperreports.engine.JRException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import se.codemate.reporting.jasperreports.xstream.RemoteNodeConverter;
import se.codemate.reporting.jasperreports.xstream.RemoteRelationshipConverter;
import se.codemate.reporting.jasperreports.xstream.RemoteMapPropertyContainerConverter;
import se.codemate.neo4j.MapPropertyContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteHTTPClient {

    private String url;
    private DefaultHttpClient client;
    private XStream xstream;

    public RemoteHTTPClient(String url, String username, String password) {

        this.url = url;

        client = new DefaultHttpClient();
        client.setCookieStore(new BasicCookieStore());
        client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        xstream = new XStream();
        xstream.setMode(XStream.NO_REFERENCES);

        xstream.alias("node", Node.class);
        xstream.alias("relationship", Relationship.class);
        xstream.alias("mapPropertyContainer", MapPropertyContainer.class);

        xstream.registerConverter(new RemoteNodeConverter(xstream.getMapper()));
        xstream.registerConverter(new RemoteRelationshipConverter(xstream.getMapper()));
        xstream.registerConverter(new RemoteMapPropertyContainerConverter(xstream.getMapper()));

    }

    public void setProxy(String proxy) {
        String[] fields = proxy.split(":");
        HttpHost proxyHost = new HttpHost(fields[0], Integer.parseInt(fields[1]), "http");
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
    }

    @SuppressWarnings(value = "unchecked")
    public Object getObject(String path, String query) throws JRException {

        try {

            HttpPost post = new HttpPost(url + path);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("query", query));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                Object object = xstream.fromXML(entity.getContent());
                entity.consumeContent();
                return object;
            } else {
                return null;
            }

        } catch (IOException e) {
            throw new JRException(e);
        }

    }

    public void close() {
        client.getConnectionManager().shutdown();
    }

}
