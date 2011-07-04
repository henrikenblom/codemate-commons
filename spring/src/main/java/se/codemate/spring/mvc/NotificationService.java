package se.codemate.spring.mvc;

public interface NotificationService {
    public void sendNotification(String message, Exception exception);
}