package se.codemate.reporting.jasperreports;

import com.jaspersoft.ireport.designer.FieldsProvider;
import com.jaspersoft.ireport.designer.FieldsProviderEditor;
import com.jaspersoft.ireport.designer.IReportConnection;
import com.jaspersoft.ireport.designer.data.ReportQueryDialog;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteFieldsProvider implements FieldsProvider {

    public boolean supportsGetFieldsOperation() {
        return true;
    }

    @SuppressWarnings(value = "unchecked")
    public JRField[] getFields(IReportConnection connection, JRDataset reportDataset, Map parameters) throws JRException, UnsupportedOperationException {

        Object url = parameters.get("NEO_URL");
        if (url == null) {
            throw new JRException("NEO_URL parameter is missing");
        }

        Object username = parameters.get("NEO_USERNAME");
        if (username == null) {
            throw new JRException("NEO_USERNAME parameter is missing");
        }

        Object password = parameters.get("NEO_PASSWORD");
        if (password == null) {
            throw new JRException("NEO_PASSWORD parameter is missing");
        }

        RemoteHTTPClient client = new RemoteHTTPClient(
                url.toString(),
                username.toString(),
                password.toString()
        );

        Object proxy = parameters.get("NEO_PROXY");
        if (proxy != null) {
            client.setProxy(proxy.toString());
        }

        String query = parseQuery(reportDataset.getQuery(), parameters);

        Map<String, String> map = (Map<String, String>) client.getObject("/neo/xml/report-fields.do", query);

        try {

            List<JRField> fields = new ArrayList<JRField>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                JRDesignField field = new JRDesignField();
                field.setName(entry.getKey());
                field.setValueClass(Class.forName(entry.getValue()));
                field.setValueClassName(entry.getValue());
                fields.add(field);
            }

            return fields.toArray(new JRField[fields.size()]);

        } catch (ClassNotFoundException e) {
            throw new JRException(e);
        }

    }

    public boolean supportsAutomaticQueryExecution() {
        return false;
    }

    public boolean hasQueryDesigner() {
        return false;
    }

    public boolean hasEditorComponent() {
        return false;
    }

    public String designQuery(IReportConnection iReportConnection, String s, ReportQueryDialog reportQueryDialog) throws JRException, UnsupportedOperationException {
        return null;
    }

    public FieldsProviderEditor getEditorComponent(ReportQueryDialog reportQueryDialog) {
        return null;
    }

    private String parseQuery(JRQuery query, Map paramaters) {
        JRQueryChunk[] chunks = query.getChunks();
        if (chunks != null && chunks.length > 0) {
            StringBuffer sbuffer = new StringBuffer();
            for (JRQueryChunk chunk : chunks) {
                switch (chunk.getType()) {
                    case JRQueryChunk.TYPE_PARAMETER: {
                        sbuffer.append(paramaters.get(chunk.getText()));
                        break;
                    }
                    case JRQueryChunk.TYPE_TEXT:
                    default: {
                        sbuffer.append(chunk.getText());
                        break;
                    }
                }

            }
            return sbuffer.toString();
        } else {
            return null;
        }
    }

}
