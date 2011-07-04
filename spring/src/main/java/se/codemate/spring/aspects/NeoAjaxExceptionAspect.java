package se.codemate.spring.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class NeoAjaxExceptionAspect {

    @Around(value = "execution(* se.codemate.spring.controllers.NeoAjaxController.*(..))")
    public Object remapException(ProceedingJoinPoint proceedingJoinPoint) {
        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw new NeoAjaxException(throwable);
        }
    }

}
