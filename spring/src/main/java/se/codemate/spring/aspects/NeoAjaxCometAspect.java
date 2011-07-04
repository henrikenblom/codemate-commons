package se.codemate.spring.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.cometd.Bayeux;
import org.cometd.Channel;
import org.cometd.Client;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.servlet.ModelAndView;
import se.codemate.spring.mvc.XStreamView;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configurable
@Aspect
public class NeoAjaxCometAspect {

    @Autowired
    private Bayeux bayeux;

    private Client client;
    private Channel channel;

    @PostConstruct
    public void initialize() {
        client = bayeux.newClient(this.getClass().getSimpleName() + "_" + hashCode());
        channel = bayeux.getChannel("/neo/ajax", true);
    }

    @PreDestroy
    public void destroy() {
        bayeux.removeClient(client.getId());
        client = null;
    }

    @AfterReturning(value = "execution(* se.codemate.spring.controllers.NeoAjaxController.create*(..))", returning = "mav")
    public void create(JoinPoint joinPoint, ModelAndView mav) throws Throwable {
        publish(joinPoint, mav);
    }

    @AfterReturning(value = "execution(* se.codemate.spring.controllers.NeoAjaxController.delete*(..))", returning = "mav")
    public void delete(JoinPoint joinPoint, ModelAndView mav) throws Throwable {
        publish(joinPoint, mav);
    }

    @AfterReturning(value = "execution(* se.codemate.spring.controllers.NeoAjaxController.updatePropertyContainer(..))", returning = "mav")
    public void update(JoinPoint joinPoint, ModelAndView mav) throws Throwable {
        publish(joinPoint, mav);
    }

    private void publish(JoinPoint joinPoint, ModelAndView mav) {

        Map<String, Object> map = new HashMap<String, Object>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] names = signature.getParameterNames();
        Object[] values = joinPoint.getArgs();
        for (int i = 0; i < names.length; i++) {
            if (ServletRequest.class.isInstance(values[i])) {
                ServletRequest request = (ServletRequest) values[i];
                if (request != null) {
                    Enumeration enumeration = request.getParameterNames();
                    while (enumeration.hasMoreElements()) {
                        String name = (String) enumeration.nextElement();
                        map.put(name, request.getParameter(name));
                    }
                }
            } else {
                map.put("_" + names[i], values[i]);
            }
        }
        map.put("_method", signature.getName());

        if (mav != null && mav.getModel() != null) {
            Object root = mav.getModel().get(XStreamView.XSTREAM_ROOT);
            if (root != null) {
                if (Node.class.isInstance(root)) {
                    map.put("nodeId", ((Node) root).getId());
                } else if (Relationship.class.isInstance(root)) {
                    map.put("relationshipId", ((Relationship) root).getId());
                } else {
                    map.put("root", root);
                }
            } else {
                map.put("model", mav.getModel());
            }
        }

        channel.publish(client, map, null);

    }

}
