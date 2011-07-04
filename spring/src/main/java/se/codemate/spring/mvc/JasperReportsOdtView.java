package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsOdtView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsOdtView() {
        setContentType("application/vnd.oasis.opendocument.text");
    }

    protected JRExporter createExporter() {
        return new JROdtExporter();
    }

    protected boolean useWriter() {
        return false;
    }

}
