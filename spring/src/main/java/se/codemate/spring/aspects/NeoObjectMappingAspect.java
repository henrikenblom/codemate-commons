package se.codemate.spring.aspects;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class NeoObjectMappingAspect {

    private static String XSTREAM_PREFIX = "XStream:";

    private XStream xstream = new XStream(new DomDriver());

    @Around(value = "execution(* org.neo4j.graphdb.PropertyContainer.setProperty(..))")
    public Object setProperty(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        if (isValidType(args[1])) {
            return proceedingJoinPoint.proceed();
        } else {
            Object[] remappedArgs = new Object[2];
            remappedArgs[0] = args[0];
            remappedArgs[1] = XSTREAM_PREFIX + xstream.toXML(args[1]);
            return proceedingJoinPoint.proceed(remappedArgs);
        }
    }

    private static boolean isValidType(Object object) {
        Class type = object.getClass();
        if (type.isArray()) {
            type = type.getComponentType();
        }
        return Boolean.class.equals(type) ||
                Byte.class.equals(type) ||
                Short.class.equals(type) ||
                Integer.class.equals(type) ||
                Long.class.equals(type) ||
                Float.class.equals(type) ||
                Double.class.equals(type) ||
                Character.class.equals(type) ||
                String.class.equals(type);
    }

    @Around(value = "execution(* org.neo4j.graphdb.PropertyContainer.getProperty(..))")
    public Object getProperty(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return unmapValue(proceedingJoinPoint.proceed());
    }

    private Object unmapValue(Object value) {
        if (String.class.isInstance(value)) {
            String stringValue = (String) value;
            if (stringValue.startsWith(XSTREAM_PREFIX)) {
                return xstream.fromXML(stringValue.substring(XSTREAM_PREFIX.length()));
            }
        }
        return value;
    }

}
