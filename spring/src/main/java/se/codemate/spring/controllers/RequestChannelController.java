package se.codemate.spring.controllers;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;
import se.codemate.utils.UUIDGenerator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RequestChannelController implements Controller, ServletContextAware {

    private MessageChannelTemplate template = new MessageChannelTemplate();

    private File tempDir;

    private File servletTempDir;

    private MessageChannel channel;

    private String view;

    public void setServletContext(ServletContext servletContext) {
        servletTempDir = WebUtils.getTempDir(servletContext);
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    @Required
    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public void setView(String view) {
        this.view = view;
    }

    public synchronized ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, Object> map = new HashMap<String, Object>();
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement().toString();
            map.put(name, request.getParameter(name));
        }

        if (MultipartHttpServletRequest.class.isInstance(request)) {
            ArrayList<File> files = new ArrayList<File>();
            ArrayList<String> types = new ArrayList<String>();
            File directory = new File(tempDir == null ? servletTempDir : tempDir, UUIDGenerator.generateUUID().toString());
            if (!directory.mkdirs()) {
                throw new IOException("Unable to create temporary directory");
            }
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            for (Iterator fileNameIterator = multipartRequest.getFileNames(); fileNameIterator.hasNext();) {
                MultipartFile multipartFile = multipartRequest.getFile(fileNameIterator.next().toString());
                File file = new File(directory, multipartFile.getOriginalFilename());
                multipartFile.transferTo(file);
                files.add(file);
                types.add(multipartFile.getContentType());
            }
            map.put("multipart-files", files.toArray(new File[files.size()]));
            map.put("multipart-types", types.toArray(new String[types.size()]));
        }

        MessageBuilder messageBuilder = MessageBuilder.withPayload(map);

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement().toString();
            messageBuilder.setHeader("request.header." + name, request.getHeader(name));
        }

        template.send(messageBuilder.build(), channel);

        if (view == null) {
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("OK");
            return null;
        } else {
            return new ModelAndView(view, map);
        }

    }

}
