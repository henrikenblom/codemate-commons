package se.codemate.spring.security;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.codemate.utils.FileUtils;

public class NeoUserDetailsServiceTest {

    private static AbstractApplicationContext context;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("context.xml");
        context.registerShutdownHook();
        context.start();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        EmbeddedGraphDatabase neoService = (EmbeddedGraphDatabase) context.getBean("neoService");
        neoService.shutdown();
        String directory = neoService.getConfig().getTxModule().getTxLogDirectory();
        FileUtils.deleteDirectory(directory);
    }

    @Test(groups = {"functest"})
    public void testUserDetailsService() throws Exception {
        UserDetailsService userDetailsService = (UserDetailsService) context.getBean("userDetailsService");
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        System.out.println(userDetails);
    }

}
