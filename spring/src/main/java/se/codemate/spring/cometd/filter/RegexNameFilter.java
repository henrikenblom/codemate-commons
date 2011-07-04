package se.codemate.spring.cometd.filter;

import org.cometd.Channel;
import org.cometd.Client;
import org.mortbay.cometd.filter.RegexFilter;

import java.util.HashMap;
import java.util.Map;

public class RegexNameFilter extends RegexFilter {

    @Override
    protected Object filterMap(Client from, Channel to, Map object) {

        if (object == null)
            return null;

        Map<Object, Object> newMap = new HashMap<Object, Object>();

        for (Object key : object.keySet()) {
            newMap.put(filter(from, to, key), object.get(key));
        }
        
        return newMap;

    }

}
