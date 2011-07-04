package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsOdsView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsOdsView() {
        setContentType("application/vnd.oasis.opendocument.spreadsheet");
    }

    protected JRExporter createExporter() {
        return new JROdsExporter();
    }

    protected boolean useWriter() {
        return false;
    }

}
