package se.codemate.spring.freemarker;

import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

public class AdvancedFreeMarkerViewResolver extends FreeMarkerViewResolver {

    @Override
    protected Class requiredViewClass() {
        return AdvancedFreeMarkerView.class;
    }

}
