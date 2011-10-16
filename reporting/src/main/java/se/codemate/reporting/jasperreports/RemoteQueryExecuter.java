package se.codemate.reporting.jasperreports;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.query.JRAbstractQueryExecuter;

import java.util.List;
import java.util.Map;

public class RemoteQueryExecuter extends JRAbstractQueryExecuter {

    private RemoteHTTPClient client;

    public RemoteQueryExecuter(JRDataset dataset, Map parametersMap) {

        super(dataset, parametersMap);

        client = new RemoteHTTPClient(
                getParameterValue("NEO_URL").toString(),
                getParameterValue("NEO_USERNAME").toString(),
                getParameterValue("NEO_PASSWORD").toString()
        );

        Object proxy = getParameterValue("NEO_PROXY", true);
        if (proxy != null) {
            client.setProxy(proxy.toString());
        }

        parseQuery();

    }

    protected String getParameterReplacement(String parameterName) {
        return getParameterValue(parameterName).toString();
    }

    @SuppressWarnings(value = "unchecked")
    public JRDataSource createDatasource() throws JRException {
        List<Map<String, ?>> list = (List<Map<String, ?>>) client.getObject("/neo/xml/report-search.do", getQueryString());
        return new JRMapCollectionDataSource(list);
    }

    public void close() {
        client.close();
    }

    public boolean cancelQuery() throws JRException {
        return true;
    }

}