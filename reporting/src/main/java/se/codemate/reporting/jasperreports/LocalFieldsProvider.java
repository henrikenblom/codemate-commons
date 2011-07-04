package se.codemate.reporting.jasperreports;

import com.jaspersoft.ireport.designer.FieldsProvider;
import com.jaspersoft.ireport.designer.FieldsProviderEditor;
import com.jaspersoft.ireport.designer.IReportConnection;
import com.jaspersoft.ireport.designer.data.ReportQueryDialog;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignField;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import se.codemate.neo4j.NeoGroovy;
import se.codemate.neo4j.NeoSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalFieldsProvider implements FieldsProvider {

    private static String XSTREAM_PREFIX = "XStream:";

    private XStream xstream = new XStream(new DomDriver());

    public boolean supportsGetFieldsOperation() {
        return true;
    }

    public JRField[] getFields(IReportConnection connection, JRDataset reportDataset, Map paramaters) throws JRException, UnsupportedOperationException {

        String query = parseQuery(reportDataset.getQuery(), paramaters);

        String root = paramaters.get("NEO_ROOT").toString();

        try {

            EmbeddedGraphDatabase neo = new EmbeddedGraphDatabase(root);
            Transaction tx = neo.beginTx();

            NeoSearch neoSearch = new NeoSearch(neo);
            NeoGroovy neoGroovy = new NeoGroovy(neo, neoSearch);

            List<PropertyContainer> propertyContainers = LocalQueryExecuter.getPropertyContainer(neoSearch, neoGroovy, query);

            Map<String, Class> fieldMap = new HashMap<String, Class>();
            fieldMap.put("_type", String.class);
            fieldMap.put("_id", Long.class);

            for (PropertyContainer propertyContainer : propertyContainers) {
                for (String key : propertyContainer.getPropertyKeys()) {
                    Object value = unmapValue(propertyContainer.getProperty(key));
                    fieldMap.put(key, value.getClass());
                }
            }

            tx.success();
            tx.finish();

            neo.shutdown();

            List<JRField> fields = new ArrayList<JRField>();

            for (String fieldName : fieldMap.keySet()) {
                JRDesignField field = new JRDesignField();
                field.setName(fieldName);
                field.setValueClass(fieldMap.get(fieldName));
                field.setValueClassName(fieldMap.get(fieldName).getName());
                fields.add(field);
            }


            return fields.toArray(new JRField[fields.size()]);

        } catch (Exception e) {
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

    private Object unmapValue(Object value) {
        if (String.class.isInstance(value)) {
            String stringValue = (String) value;
            if (stringValue.startsWith(XSTREAM_PREFIX)) {
                return xstream.fromXML(stringValue.substring(XSTREAM_PREFIX.length()));
            }
        }
        return value;
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
