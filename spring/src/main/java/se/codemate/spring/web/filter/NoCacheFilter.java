package se.codemate.spring.web.filter;

import org.springframework.web.filter.OncePerRequestFilter;
import se.codemate.utils.UUIDGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoCacheFilter extends OncePerRequestFilter {

    private long timezoneOffset;
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", new Locale("en", "US"));

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        Calendar now = Calendar.getInstance();
        timezoneOffset = now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET);
    }

    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final Date currentTime = new Date(System.currentTimeMillis() - timezoneOffset);

        response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        response.setHeader("Last-Modified", formatter.format(currentTime));
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, no-transform, max-age=0, post-check=0, pre-check=0");
        response.addHeader("Pragma", "no-cache");
        response.setHeader("Vary", "*");
        response.setHeader("ETag", UUIDGenerator.generateUUID().toString());

        filterChain.doFilter(request, response);

    }

}
