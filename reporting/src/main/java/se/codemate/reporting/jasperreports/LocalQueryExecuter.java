package se.codemate.reporting.jasperreports;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;
import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import se.codemate.neo4j.NeoGroovy;
import se.codemate.neo4j.NeoSearch;
import se.codemate.neo4j.MapPropertyContainer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LocalQueryExecuter extends JRAbstractQueryExecuter {

    private static String XSTREAM_PREFIX = "XStream:";

    private XStream xstream = new XStream(new DomDriver());

    private String root;

    public LocalQueryExecuter(JRDataset dataset, Map parametersMap) {
        super(dataset, parametersMap);
        this.root = getParameterValue("NEO_ROOT").toString();
    }

    protected String getParameterReplacement(String parameterName) {
        return getParameterValue(parameterName).toString();
    }

    public JRDataSource createDatasource() throws JRException {

        try {

            EmbeddedGraphDatabase neo = new EmbeddedGraphDatabase(root);
            Transaction tx = neo.beginTx();

            NeoSearch neoSearch = new NeoSearch(neo);
            NeoGroovy neoGroovy = new NeoGroovy(neo, neoSearch);

            List<PropertyContainer> propertyContainers = getPropertyContainer(neoSearch, neoGroovy, getQueryString());

            List<Map<String, Object>> maps = new LinkedList<Map<String, Object>>();

            for (PropertyContainer propertyContainer : propertyContainers) {

                Map<String, Object> map = new HashMap<String, Object>();

                if (Node.class.isInstance(propertyContainer)) {
                    map.put("_type", "node");
                } else if (Relationship.class.isInstance(propertyContainer)) {
                    map.put("_type", "relationship");
                } else if (MapPropertyContainer.class.isInstance(propertyContainer)) {
                    map.put("_type", "map");
                } else {
                    map.put("_type", "unknown");
                }

                if (Node.class.isInstance(propertyContainer)) {
                    map.put("_id", ((Node) propertyContainer).getId());
                } else if (Relationship.class.isInstance(propertyContainer)) {
                    map.put("_id", ((Relationship) propertyContainer).getId());
                } else if (MapPropertyContainer.class.isInstance(propertyContainer)) {
                    map.put("_id", ((MapPropertyContainer) propertyContainer).getId());
                } else {
                    map.put("_id", -1);
                }

                for (String key : propertyContainer.getPropertyKeys()) {
                    map.put(key, unmapValue(propertyContainer.getProperty(key)));
                }

                maps.add(map);

            }

            tx.success();
            tx.finish();

            neo.shutdown();

            return new JRMapCollectionDataSource(maps);

        } catch (Exception e) {
            throw new JRException(e);
        }

    }

    public void close() {
    }

    public boolean cancelQuery() throws JRException {
        return true;
    }

    private Object unmapValue(Object value) {
        if (String.class.isInstance(value)) {
            String stringValue = (String) value;
            if (stringValue.startsWith(XSTREAM_PREFIX)) {
                return xstream.fromXML(stringValue.substring(XSTREAM_PREFIX.length()));
            }
        }
        return value;
    }

    protected static List<PropertyContainer> getPropertyContainer(NeoSearch neoSearch, NeoGroovy neoGroovy, String query) throws IOException, ParseException {

        boolean getStartNodes = false;
        if (query.startsWith("<") && query.endsWith(">")) {
            getStartNodes = true;
            query = query.substring(1, query.length() - 1).trim();
        }

        boolean groovy = false;
        if (query.startsWith("GROOVY {") && query.endsWith("}")) {
            groovy = true;
            query = query.substring(8, query.length() - 1).trim();
        }

        List<PropertyContainer> propertyContainers = new LinkedList<PropertyContainer>();

        for (PropertyContainer propertyContainer : groovy ? neoGroovy.evaluate(query) : neoSearch.getPropertyContainers(query)) {
            if (Relationship.class.isInstance(propertyContainer)) {
                Relationship relationship = (Relationship) propertyContainer;
                propertyContainers.add(getStartNodes ? relationship.getStartNode() : relationship.getEndNode());
            } else {
                propertyContainers.add(propertyContainer);
            }
        }

        return propertyContainers;

    }

}
