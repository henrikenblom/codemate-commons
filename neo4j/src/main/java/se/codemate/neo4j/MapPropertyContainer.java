package se.codemate.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

public class MapPropertyContainer implements PropertyContainer {

    Map<String, Object> map = new HashMap<String, Object>();

    public GraphDatabaseService getGraphDatabase() {
        return null;
    }

    public long getId() {
        return map.hashCode();
    }

    public synchronized boolean hasProperty(String key) {
        return map.containsKey(key);
    }

    public synchronized Object getProperty(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            throw new NotFoundException(key + " property not found for Map#" + this.hashCode());
        }
    }

    public synchronized Object getProperty(String key, Object defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return defaultValue;
        }
    }

    public synchronized void setProperty(String key, Object value) {
        map.put(key, value);
    }

    public synchronized Object removeProperty(String key) {
        return map.remove(key);
    }

    public synchronized Iterable<String> getPropertyKeys() {
        return new LinkedList<String>(map.keySet());
    }

    public synchronized Iterable<Object> getPropertyValues() {
        return new LinkedList<Object>(map.values());
    }

    @Override
    public String toString() {
        return "Map[" + getId() + " " + map.toString() + "]";
    }

}