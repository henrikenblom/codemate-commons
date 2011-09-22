package se.codemate.spring.cometd.continuation;

import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.ContinuationCometdServlet;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import java.util.Iterator;
import java.util.Map;

public class SpringContinuationCometdServlet extends ContinuationCometdServlet {

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        synchronized (SpringContinuationCometdServlet.class) {

            Map<String, AbstractBayeux> hBayeuxBeans = (Map<String, AbstractBayeux>) WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBeansOfType(AbstractBayeux.class);

            if (hBayeuxBeans == null || hBayeuxBeans.size() != 1) {
                throw new ServletException("Unable to find a unique Spring bean of the type AbstractBayeux.");
            }

            Iterator<AbstractBayeux> iterator = hBayeuxBeans.values().iterator();
            if (iterator.hasNext()) {
                _bayeux = iterator.next();
            }

        }

    }

}
