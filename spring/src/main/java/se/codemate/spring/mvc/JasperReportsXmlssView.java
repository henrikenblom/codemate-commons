package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.xmlss.JRXmlssExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsXmlssView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsXmlssView() {
        setContentType("text/xml");
    }

    protected JRExporter createExporter() {
        return new JRXmlssExporter();
    }

    protected boolean useWriter() {
        return true;
    }

}
