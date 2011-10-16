package se.codemate.spring.integration.adapter.xmpp;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

@MessageEndpoint
public class XMPPEchoTest {

    private static boolean done = false;

    private static AbstractApplicationContext context;

    private long timeout;
    private long endTime;

    @BeforeClass(alwaysRun = true)
    @Parameters({"echo.timeout"})
    public void setUp(String timeOut) throws Exception {
        context = new ClassPathXmlApplicationContext("context-integration.xml");
        context.registerShutdownHook();
        context.start();
        this.timeout = timeOut == null ? (60 * 60 * 1000) : Long.parseLong(timeOut); // Defaults to 1 hour
        this.endTime = System.currentTimeMillis() + this.timeout;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        context.stop();
    }

    @Test(groups = {"functest"})
    public void testSource() throws Exception {
        while (!done && System.currentTimeMillis() < endTime) {
            Thread.sleep(1000);
        }
    }

    @ServiceActivator(inputChannel = "xmppInputChannel", outputChannel = "xmppOutputChannel")
    public Message handle(String body, @Header("to") String to, @Header("from") String from) {
        endTime = System.currentTimeMillis() + timeout;
        done = "quit".equalsIgnoreCase(body);
        if ("time".equalsIgnoreCase(body.trim())) {
            return MessageBuilder.withPayload(new Date().toString()).setHeader("to", from).setHeader("from", to).build();
        } else {
            return MessageBuilder.withPayload(body).setHeader("to", from).setHeader("from", to).build();
        }
    }

}