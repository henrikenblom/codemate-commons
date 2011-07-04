package se.codemate.reporting.jasperreports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;
import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import se.codemate.neo4j.NeoGroovy;
import se.codemate.neo4j.NeoSearch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JRNeoQueryExecuter extends JRAbstractQueryExecuter {

    private final GraphDatabaseService neo;
    private final NeoSearch neoSearch;
    private final NeoGroovy neoGroovy;

    private Transaction tx;

    public JRNeoQueryExecuter(JRDataset dataset, Map parameters) {
        super(dataset, parameters);
        neo = (GraphDatabaseService) getParameterValue(JRNeoQueryExecuterFactory.PARAMETER_NEO);
        neoSearch = (NeoSearch) getParameterValue(JRNeoQueryExecuterFactory.PARAMETER_NEO_SEARCH);
        neoGroovy = new NeoGroovy(neo, neoSearch);
        parseQuery();
    }

    protected String getParameterReplacement(String parameterName) {
        return String.valueOf(getParameterValue(parameterName));
    }

    public JRDataSource createDatasource() throws JRException {
        tx = neo.beginTx();
        try {
            List<PropertyContainer> propertyContainers = getPropertyContainer(neoSearch, neoGroovy, getQueryString());
            return new JRNeoDataSource(propertyContainers);
        } catch (IOException exception) {
            throw new JRException(exception);
        } catch (ParseException exception) {
            throw new JRException(exception);
        }
    }

    public void close() {
        if (tx != null) {
            tx.success();
            tx.finish();
        }
    }

    public boolean cancelQuery() throws JRException {
        return true;
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
