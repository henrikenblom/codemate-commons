package se.codemate.spring.mvc;

import com.thoughtworks.xstream.XStream;
import org.springframework.web.servlet.View;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class XStreamView implements View {

    public static final String XSTREAM_ROOT = "_xstream_root";

    private XStream xstream;

    private String contentType;

    private String prefix;
    private String suffix;

    public XStreamView(XStream xstream, String contentType) {
        this.xstream = xstream;
        this.contentType = contentType;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getContentType() {
        return contentType;
    }

    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(contentType);
        ServletOutputStream outputStream = response.getOutputStream();
        if (prefix != null) {
            outputStream.print(prefix);
        }
        if (model.containsKey(XSTREAM_ROOT)) {
            xstream.toXML(model.get(XSTREAM_ROOT), outputStream);
        } else {
            xstream.toXML(model, outputStream);
        }
        if (suffix != null) {
            outputStream.print(suffix);
        }
        outputStream.flush();
    }

}
