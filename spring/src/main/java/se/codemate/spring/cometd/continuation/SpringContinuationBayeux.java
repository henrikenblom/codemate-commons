package se.codemate.spring.cometd.continuation;


import org.mortbay.cometd.continuation.ContinuationBayeux;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;

public class SpringContinuationBayeux extends ContinuationBayeux implements ServletContextAware, InitializingBean {

    protected ServletContext servletContext = null;

    protected List<SpringContinuationDataFilter> filters;

    protected long maxInterval = 10000;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public List<SpringContinuationDataFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SpringContinuationDataFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }

    public void afterPropertiesSet() throws Exception {
        initialize(servletContext);
        super.setMaxInterval(maxInterval);
        if (filters != null) {
            for (SpringContinuationDataFilter filter : filters) {
                getChannel(filter.getChannel(), true).addDataFilter(filter);
            }
        }
    }

}
