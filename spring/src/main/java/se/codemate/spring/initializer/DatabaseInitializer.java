package se.codemate.spring.initializer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

public class DatabaseInitializer implements ApplicationContextAware, InitializingBean {

    private SimpleJdbcTemplate simpleJdbcTemplate;
    private String script;

    private ApplicationContext applicationContext;

    public DatabaseInitializer(SimpleJdbcTemplate simpleJdbcTemplate) {
        this.simpleJdbcTemplate = simpleJdbcTemplate;
    }

    @Required
    public void setScript(String script) {
        this.script = script;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        Resource resource = applicationContext.getResource(script);
        Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.resetSyntax();
        tokenizer.wordChars('\u0000', '\u00FF');
        tokenizer.whitespaceChars(';', ';');
        tokenizer.commentChar('/');
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            String sql = tokenizer.sval.trim();
            if (sql.length() > 0) {
                simpleJdbcTemplate.update(sql);
            }
        }
        reader.close();
    }

}