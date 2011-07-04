package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsXmlView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsXmlView() {
        setContentType("text/xml");
    }

    protected JRExporter createExporter() {
        return new JRXmlExporter();
    }

    protected boolean useWriter() {
        return true;
    }

}
