package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsTextView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsTextView() {
        setContentType("text/plain");
    }

    protected JRExporter createExporter() {
        return new JRTextExporter();
    }

    protected boolean useWriter() {
        return true;
    }

}