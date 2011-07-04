package se.codemate.spring.controllers;


import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultController implements Controller {

    private static final int MAX_CACHE_SIZE = 100;

    private static Map<String, ModelAndView> cache = new LinkedHashMap<String, ModelAndView>() {
        protected boolean removeEldestEntry(Map.Entry<String, ModelAndView> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public synchronized ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        if (cache.containsKey(request.getRequestURI())) {
            return cache.get(request.getRequestURI());
        } else {
            ModelAndView mav = new ModelAndView();
            cache.put(request.getRequestURI(), mav);
            return mav;
        }
    }

}