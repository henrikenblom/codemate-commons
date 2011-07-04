package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsRtfView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsRtfView() {
        setContentType("application/rtf");
    }

    protected JRExporter createExporter() {
        return new JRRtfExporter();
    }

    protected boolean useWriter() {
        return true;
    }

}
