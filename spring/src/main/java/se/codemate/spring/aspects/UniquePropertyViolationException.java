package se.codemate.spring.aspects;

import org.springframework.core.NestedRuntimeException;

public class UniquePropertyViolationException extends NestedRuntimeException {

    public UniquePropertyViolationException(String msg) {
        super(msg);
    }

    public UniquePropertyViolationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
