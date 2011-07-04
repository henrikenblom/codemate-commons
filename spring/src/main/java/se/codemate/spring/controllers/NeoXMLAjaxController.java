package se.codemate.spring.controllers;

import com.thoughtworks.xstream.XStream;
import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import se.codemate.neo4j.*;
import se.codemate.spring.mvc.ModelMapConverter;
import se.codemate.spring.mvc.XStreamView;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@Controller
public class NeoXMLAjaxController {

    @Resource
    private GraphDatabaseService neo;

    @Resource
    private NeoSearch neoSearch;

    private NeoGroovy neoGroovy;

    private NeoUtils neoUtils;

    private XStream xstream = new XStream();

    private XStreamView xstreamView;

    private XStreamXMLNodeConverter nodeConverter;

    @PostConstruct
    public void initialize() {

        neoUtils = new NeoUtils(neo);

        neoGroovy = new NeoGroovy(neo, neoSearch);

        xstream.setMode(XStream.NO_REFERENCES);

        xstream.registerConverter(new ModelMapConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
        nodeConverter = new XStreamXMLNodeConverter(neo, xstream.getMapper());
        xstream.registerConverter(nodeConverter);
        xstream.registerConverter(new XStreamXMLRelationshipConverter(neo, xstream.getMapper()));
        xstream.registerConverter(new XStreamXMLMapPropertyContainerConverter(xstream.getMapper()));

        xstream.alias("model", ModelMap.class);
        xstream.alias("node", Node.class);
        xstream.alias("relationship", Relationship.class);
        xstream.alias("mapPropertyContainer", MapPropertyContainer.class);

        try {
            xstream.alias("node", Class.forName("org.neo4j.kernel.impl.core.NodeImpl"));
            xstream.alias("node", Class.forName("org.neo4j.kernel.impl.core.NodeProxy"));
        } catch (ClassNotFoundException e) {
            // no-op
        }

        try {
            xstream.alias("relationship", Class.forName("org.neo4j.kernel.impl.core.RelationshipImpl"));
            xstream.alias("relationship", Class.forName("org.neo4j.kernel.impl.core.RelationshipProxy"));
        } catch (ClassNotFoundException e) {
            // no-op
        }

        xstreamView = new XStreamView(xstream, "text/xml");
        xstreamView.setPrefix("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    }

    @RequestMapping(value = {"/neo/xml/get_node.do"})
    public ModelAndView getNode(@RequestParam("id") Long id) throws IOException {
        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, neo.getNodeById(id));
        return mav;
    }

    @RequestMapping(value = {"/neo/xml/set_node.do"})
    public synchronized ModelAndView setNode(HttpServletRequest request) throws IOException, ClassNotFoundException {
        BufferedReader reader = request.getReader();
        try {
            ModelAndView mav = new ModelAndView(xstreamView);
            nodeConverter.resetNodeIdMap();
            mav.addObject(XStreamView.XSTREAM_ROOT, xstream.fromXML(reader));
            return mav;
        } finally {
            reader.close();
        }
    }

    @RequestMapping(value = {"/neo/xml/delete_node.do"})
    public ModelAndView deleteNode(@RequestParam("id") Long id) throws IOException {

        Set<Long> ids = neoUtils.deleteNode(neo.getNodeById(id));

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject("id", id);
        mav.addObject("deleted", ids.size() > 0);
        mav.addObject("deletedIDs", ids);

        return mav;

    }

    @RequestMapping(value = {"/neo/xml/get_relationship.do"})
    public ModelAndView getRelationship(@RequestParam("id") Long id) throws IOException {
        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, neo.getRelationshipById(id));
        return mav;
    }

    @RequestMapping(value = {"/neo/xml/set_relationship.do"})
    public synchronized ModelAndView setRelationship(HttpServletRequest request) throws IOException, ClassNotFoundException {
        BufferedReader reader = request.getReader();
        try {
            ModelAndView mav = new ModelAndView(xstreamView);
            nodeConverter.resetNodeIdMap();
            mav.addObject(XStreamView.XSTREAM_ROOT, xstream.fromXML(reader));
            return mav;
        } finally {
            reader.close();
        }
    }

    @RequestMapping(value = {"/neo/xml/delete_relationship.do"})
    public synchronized ModelAndView deleteRelationship(@RequestParam("id") Long id) throws IOException, ClassNotFoundException {

        Relationship relationship = neo.getRelationshipById(id);
        neoUtils.deleteRelationship(relationship);

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject("id", id);
        mav.addObject("deleted", true);
        return mav;

    }

    @RequestMapping(value = {"/neo/xml/get_graph.do"})
    public ModelAndView getGraph(@RequestParam(value = "id", required = false) Long id) throws IOException {

        Node node = id == null ? neo.getReferenceNode() : neo.getNodeById(id);

        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(node);

        Set<Node> nodes = new HashSet<Node>();
        Set<Relationship> relationships = new HashSet<Relationship>();

        while (!queue.isEmpty()) {
            Node currentNode = queue.removeFirst();
            if (nodes.add(currentNode)) {
                for (Relationship relationship : currentNode.getRelationships(Direction.OUTGOING)) {
                    if (relationships.add(relationship)) {
                        queue.add(relationship.getEndNode());
                    }
                }
            }
        }

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject("nodes", nodes);
        mav.addObject("relationships", relationships);
        return mav;

    }


    @RequestMapping(value = {"/neo/xml/set_graph.do"})
    public synchronized ModelAndView setGraph(HttpServletRequest request) throws IOException, ClassNotFoundException {
        BufferedReader reader = request.getReader();
        try {
            ModelAndView mav = new ModelAndView(xstreamView);
            nodeConverter.resetNodeIdMap();
            xstream.fromXML(reader);
            mav.addObject(XStreamView.XSTREAM_ROOT, nodeConverter.getNodeIdMap());
            return mav;
        } finally {
            reader.close();
        }
    }

    @RequestMapping(value = {"/neo/xml/search.do"})
    public ModelAndView search(@RequestParam("query") String query, @RequestParam(value = "type", required = false) String type) throws IOException, ParseException {
        ModelAndView mav = new ModelAndView(xstreamView);
        if ("relationship".equalsIgnoreCase(type)) {
            mav.addObject(XStreamView.XSTREAM_ROOT, neoSearch.getRelationships(query));
        } else if ("node".equalsIgnoreCase(type)) {
            mav.addObject(XStreamView.XSTREAM_ROOT, neoSearch.getNodes(query));
        } else {
            mav.addObject(XStreamView.XSTREAM_ROOT, neoSearch.getPropertyContainers(query));
        }
        return mav;
    }

    @RequestMapping(value = {"/neo/xml/report-search.do"})
    public ModelAndView resportSearch(@RequestParam("query") String query) throws IOException, ParseException {
        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, getPropertyContainers(query));
        return mav;
    }

    @RequestMapping(value = {"/neo/xml/report-fields.do"})
    public ModelAndView fields(@RequestParam("query") String query) throws IOException, ParseException {

        Map<String, String> map = new TreeMap<String, String>();

        map.put("_type", "java.lang.String");
        map.put("_id", "java.lang.Long");

        for (PropertyContainer propertyContainer : getPropertyContainers(query)) {
            for (String key : propertyContainer.getPropertyKeys()) {
                Object value = propertyContainer.getProperty(key, null);
                if (value != null) {
                    map.put(key, value.getClass().getName());
                }
            }
        }

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, map);
        return mav;

    }

    private List<PropertyContainer> getPropertyContainers(String query) throws IOException, ParseException {

        query = query.trim();

        boolean getStartNodes = false;
        if (query.startsWith("<") && query.endsWith(">")) {
            getStartNodes = true;
            query = query.substring(1, query.length() - 1).trim();
        }

        boolean groovy = false;
        if (query.startsWith("GROOVY {") && query.endsWith("}")) {
            groovy = true;
            query = query.substring(8, query.length() - 1).trim();
        }

        List<PropertyContainer> propertyContainers = new LinkedList<PropertyContainer>();

        for (PropertyContainer propertyContainer : groovy ? neoGroovy.evaluate(query) : neoSearch.getPropertyContainers(query)) {
            if (Relationship.class.isInstance(propertyContainer)) {
                Relationship relationship = (Relationship) propertyContainer;
                propertyContainers.add(getStartNodes ? relationship.getStartNode() : relationship.getEndNode());
            } else {
                propertyContainers.add(propertyContainer);
            }
        }

        return propertyContainers;

    }

}
