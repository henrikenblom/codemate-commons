package se.codemate.spring.mvc;

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class JasperReportsHtmlView extends AbstractJasperReportsSingleFormatView {

    public JasperReportsHtmlView() {
        setContentType("text/html");
    }

    protected JRExporter createExporter() {
        return new JRHtmlExporter();
    }

    protected boolean useWriter() {
        return true;
    }

    @Override
    protected void renderReport(JasperPrint populatedReport, Map model, HttpServletResponse response) throws Exception {

        if (model.containsKey("REQUEST_OBJECT")) {
            HttpServletRequest request = (HttpServletRequest) model.get("REQUEST_OBJECT");
            request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, populatedReport);
        }

        super.renderReport(populatedReport, model, response);

    }

}
