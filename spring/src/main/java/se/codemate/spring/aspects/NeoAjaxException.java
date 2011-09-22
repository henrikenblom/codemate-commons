package se.codemate.spring.aspects;

import org.springframework.core.NestedRuntimeException;

public class NeoAjaxException extends NestedRuntimeException {

    public NeoAjaxException(String msg) {
        super(msg);
    }

    public NeoAjaxException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public NeoAjaxException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

