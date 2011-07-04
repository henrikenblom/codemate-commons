package se.codemate.spring.controllers;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.apache.lucene.queryParser.ParseException;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

@Controller
public class NeoAjaxController {

    private static String TYPE_NODE = "node";
    private static String TYPE_RELATIONSHIP = "relationship";

    @Resource
    private GraphDatabaseService neo;

    @Resource
    private NeoSearch neoSearch;

    private NeoUtils neoUtils;

    private XStreamView xstreamView;

    @PostConstruct
    public void initialize() {

        neoUtils = new NeoUtils(neo);

        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        xstream.setMode(XStream.NO_REFERENCES);

        xstream.registerConverter(new ModelMapConverter(xstream.getMapper()), XStream.PRIORITY_VERY_HIGH);
        xstream.registerConverter(new XStreamNodeConverter(neo, xstream.getMapper()));
        xstream.registerConverter(new XStreamRelationshipConverter(neo, xstream.getMapper()));

        xstream.alias("model", ModelMap.class);
        xstream.alias("node", Node.class);
        xstream.alias("relationship", Relationship.class);

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

        xstreamView = new XStreamView(xstream, "text/json");

    }

    @RequestMapping(value = {"/neo/ajax/create_relationship.do"})
    public ModelAndView createRelationship(HttpServletRequest request,
                                           @RequestParam("_startNodeId") Long startNodeId,
                                           @RequestParam(value = "_endNodeId", required = false) Long endNodeId,
                                           @RequestParam("_type") String type) {

        Node startNode = neo.getNodeById(startNodeId);

        Node endNode;
        if (endNodeId == null) {
            endNode = neo.createNode();
        } else {
            endNode = neo.getNodeById(endNodeId);
        }

        Relationship relationship = startNode.createRelationshipTo(endNode, new SimpleRelationshipType(type));

        Enumeration enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            if (!name.startsWith("_")) {
                String[] fields = name.split(":");
                String key = fields[0];
                Object value = NeoUtils.toPrimitive(request.getParameter(name), fields.length > 1 ? fields[1] : null, fields.length > 2 ? fields[2] : null);
                relationship.setProperty(key, value);
            }
        }

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, relationship);
        return mav;

    }

    @RequestMapping(value = {"/neo/ajax/get_relationship.do"})
    public ModelAndView getRelationship(@RequestParam("_relationshipId") Long relationshipId) {
        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, neo.getRelationshipById(relationshipId));
        return mav;
    }

    @RequestMapping(value = {"/neo/ajax/update_relationship.do"})
    public ModelAndView updateRelationship(HttpServletRequest request,
                                           @RequestParam("_relationshipId") Long relationshipId,
                                           @RequestParam(value = "_strict", required = false) Boolean strict) {
        return updatePropertyContainer(request, relationshipId, TYPE_RELATIONSHIP, strict);
    }

    @RequestMapping(value = {"/neo/ajax/delete_relationship.do"})
    public ModelAndView deleteRelationship(@RequestParam("_relationshipId") Long relationshipId) {

        Relationship relationship = neo.getRelationshipById(relationshipId);
        neoUtils.deleteRelationship(relationship);

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject("id", relationshipId);
        mav.addObject("deleted", true);
        return mav;

    }

    @RequestMapping(value = {"/neo/ajax/create_node.do"})
    public ModelAndView createNode() throws IOException {
        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, neo.createNode());
        return mav;
    }

    @RequestMapping(value = {"/neo/ajax/get_node.do"})
    public ModelAndView getNode(@RequestParam("_nodeId") Long nodeId) throws IOException {
        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, neo.getNodeById(nodeId));
        return mav;
    }

    @RequestMapping(value = {"/neo/ajax/update_node.do"})
    public ModelAndView updateNode(HttpServletRequest request,
                                   @RequestParam("_nodeId") Long nodeId,
                                   @RequestParam(value = "_strict", required = false) Boolean strict) {
        return updatePropertyContainer(request, nodeId, TYPE_NODE, strict);
    }

    @RequestMapping(value = {"/neo/ajax/delete_node.do"})
    public ModelAndView deleteNode(@RequestParam("_nodeId") Long nodeId) {

        Set<Long> ids = neoUtils.deleteNode(neo.getNodeById(nodeId));

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject("id", nodeId);
        mav.addObject("deleted", ids.size() > 0);
        mav.addObject("deletedIDs", ids);
        return mav;

    }

    @RequestMapping(value = {"/neo/ajax/update_properties.do"})
    public ModelAndView updatePropertyContainer(HttpServletRequest request,
                                                @RequestParam("_id") Long id,
                                                @RequestParam("_type") String type,
                                                @RequestParam(value = "_strict", required = false) Boolean strict) {

        PropertyContainer propertyContainer = TYPE_NODE.equalsIgnoreCase(type.trim()) ? neo.getNodeById(id) : neo.getRelationshipById(id);

        Set<String> keySet = null;
        if (strict != null && strict) {
            keySet = NeoUtils.getKeysSet(propertyContainer);
        }

        Enumeration enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            if (!name.startsWith("_")) {
                String[] fields = name.split(":");
                String key = fields[0];
                String primitiveType = fields.length > 1 ? fields[1] : null;
                String pattern = fields.length > 2 ? fields[2] : null;
                if ("relationship".equalsIgnoreCase(primitiveType)) {
                    Node node = (Node) propertyContainer;
                    long otherId = Long.parseLong(request.getParameter(name));
                    boolean relationshipExists = false;
                    RelationshipType relationshipType = new SimpleRelationshipType(key);
                    for (Relationship relationship : node.getRelationships(relationshipType, Direction.OUTGOING)) {
                        if (relationship.getEndNode().getId() == otherId) {
                            relationshipExists = true;
                        } else {
                            relationship.delete();
                        }
                    }
                    if (!relationshipExists && otherId != -1) {
                        node.createRelationshipTo(neo.getNodeById(otherId), relationshipType);
                    }
                } else if ("checkbox".equalsIgnoreCase(primitiveType)) {
                    String[] values = request.getParameterValues(name);
                    Set<String> propertyKeys = NeoUtils.getKeysSet(propertyContainer);
                    for (String propertyKey : propertyKeys) {
                        if (propertyKey.startsWith(key + "#")) {
                            propertyContainer.removeProperty(propertyKey);
                        }
                    }
                    for (String value : values) {
                        propertyContainer.setProperty(key + "#" + value, true);
                    }
                } else {
                    if (primitiveType == null) {
                        Object oldValue = propertyContainer.getProperty(key, null);
                        if (oldValue != null) {
                            primitiveType = oldValue.getClass().getSimpleName();
                        }
                    }
                    if (request.getParameter(name) != null && request.getParameter(name).length() > 0) {
                        Object value = NeoUtils.toPrimitive(request.getParameter(name), primitiveType, pattern);
                        propertyContainer.setProperty(key, value);
                        if (keySet != null) {
                            keySet.remove(key);
                        }
                    }
                }
            }
        }

        if (keySet != null) {
            for (String key : keySet) {
                propertyContainer.removeProperty(key);
            }
        }

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject(XStreamView.XSTREAM_ROOT, propertyContainer);
        return mav;

    }

    @RequestMapping(value = {"/neo/ajax/update_credentials.do"})
    public ModelAndView updateUserCredentials(HttpServletRequest request,
                                              @RequestParam("_id") Long id,
                                              @RequestParam("username") String username,
                                              @RequestParam("password_confirm_1") String password,
                                              @RequestParam("password_confirm_2") String passwordConfirmation) throws IOException, NoSuchAlgorithmException {

        Node node = neo.getNodeById(id);

        node.setProperty("username", username);

        if (!"00000000".equals(password) && password.equals(passwordConfirmation)) {

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            byte messageDigest[] = sha256.digest(password.getBytes("UTF-8"));

            StringBuffer passwordDigest = new StringBuffer();
            for (byte b : messageDigest) {
                int i = b & 0xff;
                if (i < 16) {
                    passwordDigest.append('0');
                }
                passwordDigest.append(Integer.toHexString(i));
            }

            node.setProperty("password", passwordDigest.toString());

        }

        RelationshipType relationshipType = new SimpleRelationshipType("HAS_ROLE");
        for (Relationship relationship : node.getRelationships(relationshipType)) {
            relationship.delete();
        }

        String[] roles = request.getParameterValues("role:checkbox");
        if (roles != null) {
            for (String role : roles) {
                try {
                    List<Node> authorities = neoSearch.getNodes("authority:" + role);
                    for (Node authority : authorities) {
                        node.createRelationshipTo(authority, relationshipType);
                    }
                } catch (ParseException exception) {
                    /* no-op */
                }
            }
        }

        ModelAndView mav = new ModelAndView(xstreamView);
        mav.addObject("id", id);
        mav.addObject("updated", true);
        return mav;

    }

    @RequestMapping(value = {"/neo/ajax/get_node_view.do"})
    public ModelAndView getNodeView(@RequestParam("_nodeId") Long nodeId,
                                    @RequestParam(value = "_class", required = false) String viewClass,
                                    @RequestParam(value = "_variant", required = false) String variant) {

        return getView(nodeId, TYPE_NODE, viewClass, variant);

    }

    @RequestMapping(value = {"/neo/ajax/get_relationship_view.do"})
    public ModelAndView getRelationshipView(@RequestParam("_relationshipId") Long relationshipId,
                                            @RequestParam(value = "_class", required = false) String viewClass,
                                            @RequestParam(value = "_variant", required = false) String variant) {

        return getView(relationshipId, TYPE_RELATIONSHIP, viewClass, variant);

    }

    @RequestMapping(value = {"/neo/ajax/get_view.do"})
    private ModelAndView getView(@RequestParam("_id") Long id,
                                 @RequestParam("_type") String type,
                                 @RequestParam(value = "_class", required = false) String viewClass,
                                 @RequestParam(value = "_variant", required = false) String variant) {

        PropertyContainer propertyContainer = TYPE_NODE.equalsIgnoreCase(type.trim()) ? neo.getNodeById(id) : neo.getRelationshipById(id);

        String view = viewClass == null ? (String) propertyContainer.getProperty("class", type) : viewClass;
        if (variant != null) {
            view += "_" + variant;
        }

        ModelAndView mav = new ModelAndView("/views/" + view);
        mav.addObject(type, propertyContainer);
        return mav;

    }
/*
    private String extractKey(String name) {
        if (name.indexOf(':') == -1) {
            return name;
        } else {
            return name.substring(0, name.indexOf(':'));
        }
    }

    private String extractType(String name) {
        if (name.indexOf(':') == -1) {
            return null;
        } else {
            return name.substring(name.indexOf(':') + 1);
        }
    }
*/
}
