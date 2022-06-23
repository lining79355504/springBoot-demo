package com.demo.config;

import com.dianping.cat.CatConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mort
 * @Description
 * @date 2022/5/17
 **/
public class CatAsyncInterceptor implements AsyncHandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(CatConstants.CAT_PAGE_URI, getUrl(request));
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (null != ex) {
            System.out.println("发生异常:" + ex.getMessage());
            throw ex;
        }
    }

    private String getUrl(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (StringUtils.isBlank(pattern)) {
            pattern = request.getContextPath();
        }

        String method = request.getMethod();

        return String.format("%s %s", pattern, method);
    }
}
