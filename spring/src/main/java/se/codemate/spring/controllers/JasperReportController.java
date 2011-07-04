package se.codemate.spring.controllers;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRProperties;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import se.codemate.neo4j.NeoSearch;
import se.codemate.reporting.jasperreports.JRNeoQueryExecuterFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class JasperReportController implements Controller, ApplicationContextAware, InitializingBean {

    @Resource
    protected GraphDatabaseService neo;

    @Resource
    protected NeoSearch neoSearch;

    protected ApplicationContext applicationContext;

    protected UrlPathHelper urlPathHelper = new UrlPathHelper();

    protected File reportRoot;

    private String imageServletPath;
    private String indexViewName;

    private Map<String, Set<Map<String, String>>> descriptors;

    @Required
    public void setReportRoot(String reportRoot) {
        this.reportRoot = new File(reportRoot);
    }

    @Required
    public void setImageServletPath(String imageServletPath) {
        this.imageServletPath = imageServletPath;
    }

    @Required
    public void setIndexViewName(String indexViewName) {
        this.indexViewName = indexViewName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        JRProperties.setProperty("net.sf.jasperreports.query.executer.factory.neo", "se.codemate.reporting.jasperreports.JRNeoQueryExecuterFactory");
    }

    public void afterPropertiesSet() throws Exception {
        descriptors = generateJasperDescriptors();
    }

    protected Map<String, Set<Map<String, String>>> generateJasperDescriptors() throws IOException {

        if (!reportRoot.exists()) {
            throw new IOException(reportRoot.getAbsolutePath() + " does not exist!");
        }

        if (!reportRoot.isDirectory()) {
            throw new IOException(reportRoot.getAbsolutePath() + " is not a directory!");
        }

        File[] reports = reportRoot.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jasper");
            }
        });

        Map<String, Set<Map<String, String>>> descriptors = new TreeMap<String, Set<Map<String, String>>>();

        for (File report : reports) {
            try {
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new FileInputStream(report));
                Map<String, String> descriptor = new HashMap<String, String>();
                descriptor.put("file", report.getName());
                descriptor.put("name", jasperReport.getName());
                String description = jasperReport.getProperty("description");
                String category = "default";
                if (description != null) {
                    String[] fields = description.split(":");
                    descriptor.put("description", fields[fields.length - 1]);
                    category = fields.length > 1 ? fields[0] : "default";
                }
                descriptor.put("category", category);
                Set<Map<String, String>> descriptorList = descriptors.get(category);
                if (descriptorList == null) {
                    descriptorList = new TreeSet<Map<String, String>>(new Comparator<Map<String, String>>() {
                        public int compare(Map<String, String> decriptor1, Map<String, String> decriptor2) {

                            String name1 = decriptor1.get("name");
                            String name2 = decriptor2.get("name");

                            if (name1 == null) {
                                name1 = "";
                            }
                            if (name2 == null) {
                                name2 = "";
                            }

                            return name1.toLowerCase().compareTo(name2.toLowerCase());

                        }
                    });
                    descriptors.put(category, descriptorList);
                }
                descriptorList.add(descriptor);
            } catch (JRException jre) {
                jre.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return descriptors;

    }

    protected ModelAndView generateReportModelAndView(HttpServletRequest request, String report) {

        File reportFile = new File(reportRoot, report);

        String path = reportFile.getAbsolutePath();

        String format = path.substring(path.lastIndexOf(".") + 1);
        path = path.replace("." + format, ".jasper");


        Map<String, Object> model = new HashMap<String, Object>();

        model.put("REQUEST_OBJECT", request);

        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            model.put(name, request.getParameter(name));
        }

        model.put("format", format);
        model.put(JRNeoQueryExecuterFactory.PARAMETER_NEO, neo);
        model.put(JRNeoQueryExecuterFactory.PARAMETER_NEO_SEARCH, neoSearch);

        model.put(JRParameter.REPORT_VIRTUALIZER, new JRGzipVirtualizer(100));
        if ("html".equalsIgnoreCase(format) ||
                "xls".equalsIgnoreCase(format) ||
                "ods".equalsIgnoreCase(format) ||
                "csv".equalsIgnoreCase(format)) {
            model.put(JRParameter.IS_IGNORE_PAGINATION, true);
        }

        model.put("SUBREPORT_DIR", "file://" + reportRoot.getAbsolutePath() + File.separatorChar);

        Properties mappingsWithClassNames = new Properties();

        mappingsWithClassNames.put("html", "se.codemate.spring.mvc.JasperReportsHtmlView");
        mappingsWithClassNames.put("rtf", "se.codemate.spring.mvc.JasperReportsRtfView");
        mappingsWithClassNames.put("txt", "se.codemate.spring.mvc.JasperReportsTextView");
        mappingsWithClassNames.put("xml", "se.codemate.spring.mvc.JasperReportsXmlView");
        mappingsWithClassNames.put("xmlss", "se.codemate.spring.mvc.JasperReportsXmlssView");
        mappingsWithClassNames.put("odt", "se.codemate.spring.mvc.JasperReportsOdtView");
        mappingsWithClassNames.put("ods", "se.codemate.spring.mvc.JasperReportsOdsView");

        mappingsWithClassNames.put("csv", "org.springframework.web.servlet.view.jasperreports.JasperReportsCsvView");
        mappingsWithClassNames.put("pdf", "org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView");
        mappingsWithClassNames.put("xls", "org.springframework.web.servlet.view.jasperreports.JasperReportsXlsView");

        JasperReportsMultiFormatView view = new JasperReportsMultiFormatView();
        view.setFormatMappings(mappingsWithClassNames);
        view.setUrl("file://" + path);
        view.setApplicationContext(applicationContext);

        if ("true".equalsIgnoreCase(request.getParameter("empty_ds"))) {
            model.put("reportData", new JREmptyDataSource());
            view.setReportDataKey("reportData");
        }

        if ("html".equalsIgnoreCase(format)) {
            Map<Object, Object> parameters = new HashMap<Object, Object>();
            parameters.put(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
            parameters.put(JRHtmlExporterParameter.IMAGES_URI, imageServletPath + "?image=");
            parameters.put(JRHtmlExporterParameter.HTML_HEADER, "");
            parameters.put(JRHtmlExporterParameter.HTML_FOOTER, "");
            view.setExporterParameters(parameters);
        }

        request.setAttribute(AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE, new RequestContext(request, ((WebApplicationContext) applicationContext).getServletContext(), model));

        return new ModelAndView(view, model);

    }

    public synchronized ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, JRException {

        String uri = request.getRequestURI();

        if (uri.endsWith("index.do")) {
            if ("true".equalsIgnoreCase(request.getParameter("refresh"))) {
                descriptors = generateJasperDescriptors();
            }
            ModelAndView mav = new ModelAndView(indexViewName);
            mav.addObject("reports", descriptors);
            return mav;
        } else if (uri.endsWith("delete.do")) {
            File file = new File(reportRoot, request.getParameter("file"));
            if (file.delete()) {
                descriptors = generateJasperDescriptors();
            }
            ModelAndView mav = new ModelAndView(indexViewName);
            mav.addObject("reports", descriptors);
            return mav;
        } else if (uri.endsWith("upload.do")) {

            ModelAndView mav = new ModelAndView(indexViewName);
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile multipartFile = multipartRequest.getFile("file");

            if (multipartFile != null) {
                if (multipartFile.getOriginalFilename().toLowerCase().endsWith(".jrxml")) {
                    File jasperFile = new File(reportRoot, multipartFile.getOriginalFilename().toLowerCase().replace(".jrxml", ".jasper"));
                    JasperCompileManager.compileReportToStream(multipartFile.getInputStream(), new FileOutputStream(jasperFile));
                } else if (multipartFile.getOriginalFilename().toLowerCase().endsWith(".jasper")) {
                    multipartFile.transferTo(new File(reportRoot, multipartFile.getOriginalFilename().toLowerCase()));
                }
                mav.addObject("file", multipartFile);
            }

            descriptors = generateJasperDescriptors();

            mav.addObject("reports", descriptors);

            return mav;

        } else {
            ModelAndView mav = generateReportModelAndView(request, urlPathHelper.getPathWithinServletMapping(request));
            SecurityContext securityContext = SecurityContextHolder.getContext();
            if (securityContext != null) {
                Authentication authentication = securityContext.getAuthentication();
                if (authentication != null) {
                    mav.addObject("USERNAME", authentication.getName());
                    for (GrantedAuthority authority : authentication.getAuthorities()) {
                        mav.addObject(authority.getAuthority(), "granted");
                    }
                }
            }
            return mav;
        }

    }

}