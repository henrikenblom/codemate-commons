package se.codemate.reporting.jasperreports;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.fill.JRFillParameter;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;

import java.util.Map;

public class RemoteQueryExecuterFactory implements JRQueryExecuterFactory {

    public RemoteQueryExecuterFactory() {
        System.out.println(getClass().getName());
    }

    public Object[] getBuiltinParameters() {
        return new Object[]{};
    }

    public JRQueryExecuter createQueryExecuter(JRDataset reportDataset, Map paramaters) throws JRException {
        return new RemoteQueryExecuter(reportDataset,paramaters);
    }

    public boolean supportsQueryParameterType(String string) {
        return true;
    }

}
