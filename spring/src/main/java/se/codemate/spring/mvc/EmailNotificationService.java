package se.codemate.spring.mvc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class EmailNotificationService implements NotificationService {

    private MailSender exceptionMailSender;

    private SimpleMailMessage templateExceptionMailMessage;

    public static final Log logger = LogFactory.getLog(EmailNotificationService.class);

    public void setExceptionMailSender(MailSender exceptionMailSender) {
        this.exceptionMailSender = exceptionMailSender;
    }

    public void setTemplateExceptionMailMessage(SimpleMailMessage templateExceptionMailMessage) {
        this.templateExceptionMailMessage = templateExceptionMailMessage;
    }

    public void sendNotification(String message, Exception exception) {

        SimpleMailMessage msg = new SimpleMailMessage(this.templateExceptionMailMessage);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.print("Application Message : ");
        printWriter.println(message);
        printWriter.print("Exception Class     : ");
        printWriter.println(exception.getClass().getName());
        printWriter.print("Exception Message   : ");
        printWriter.println(exception.getMessage());
        printWriter.println("-----------------------------------------------------------------------------");
        exception.printStackTrace(printWriter);
        printWriter.flush();

        msg.setText(stringWriter.toString());

        try {
            this.exceptionMailSender.send(msg);
        } catch (MailException ex) {
            logger.fatal("Email Notification message could not sent", ex);
        }

    }
}
