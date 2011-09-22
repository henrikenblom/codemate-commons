package se.codemate.spring.cometd.continuation;

import org.cometd.Channel;
import org.cometd.Client;
import org.cometd.DataFilter;
import org.mortbay.cometd.filter.JSONDataFilter;
import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class SpringContinuationDataFilter implements DataFilter, InitializingBean {

    private String channel;
    private String filterClass;
    private String init;

    private DataFilter wrappedFilter;

    public String getChannel() {
        return channel;
    }

    @Required
    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Required
    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public void afterPropertiesSet() throws Exception {
        Class filterClass = Class.forName(this.filterClass);
        wrappedFilter = (DataFilter) filterClass.newInstance();
        if (init != null && JSONDataFilter.class.isAssignableFrom(filterClass)) {
            ((JSONDataFilter) wrappedFilter).init(JSON.parse(init));
        }
    }

    public Object filter(Client client, Channel channel, Object o) throws IllegalStateException {
        return wrappedFilter.filter(client, channel, o);
    }

}
