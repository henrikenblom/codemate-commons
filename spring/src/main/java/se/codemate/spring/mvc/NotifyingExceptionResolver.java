package se.codemate.spring.mvc;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;

public class NotifyingExceptionResolver extends SimpleMappingExceptionResolver {

    private Properties priorityExceptionMappings;
    private Map<String, Integer> statusCodeMappings;
    private Map<String, NotificationService> priorityNotificationServicesMappings;
    private String notificationMessage = null;

    public void setPriorityExceptionMappings(Properties priorityExceptionMappings) {
        this.priorityExceptionMappings = priorityExceptionMappings;
    }

    public void setStatusCodeMappings(Map<String, Integer> statusCodeMappings) {
        this.statusCodeMappings = statusCodeMappings;
    }

    public void setPriorityNotificationServicesMappings(Map<String, NotificationService> priorityNotificationServicesMappings) {
        this.priorityNotificationServicesMappings = priorityNotificationServicesMappings;
    }

    public void setNotificationMessage(String message) {
        this.notificationMessage = message;
    }

    @Override
    protected Integer determineStatusCode(HttpServletRequest request, String viewName) {
        if (statusCodeMappings != null && statusCodeMappings.containsKey(viewName)) {
            return statusCodeMappings.get(viewName);
        } else {
            return super.determineStatusCode(request, viewName);
        }
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (logger.isDebugEnabled()) {
            logger.debug("An Exception has occured in the application", ex);
        } else {
            logger.warn("An Exception has occured in the application: " + ex);
        }
        sendNotification(ex);
        return super.doResolveException(request, response, handler, ex);
    }

    private void sendNotification(Exception ex) {

        String priority = resolvePriority(this.priorityExceptionMappings, ex);
        NotificationService notificationService = resolveNotificationService(this.priorityNotificationServicesMappings, priority);
        String message = (notificationMessage == null ? priority : notificationMessage);

        if (notificationService != null) {
            logger.debug("notification message was sent");
            notificationService.sendNotification(message, ex);

        }
    }

    private String resolvePriority(Properties priorityExpMappings, Exception exception) {
        return this.findMatchingViewName(priorityExpMappings, exception);
    }

    private NotificationService resolveNotificationService(Map<String, NotificationService> priorityNotificationServicesMappings, String priority) {
        NotificationService notificationService;
        notificationService = priorityNotificationServicesMappings.get(priority);
        if (notificationService != null && logger.isDebugEnabled()) {
            logger.debug("Resolving to a notification service for priority [" + priority + "]");
        }
        return notificationService;
    }
}