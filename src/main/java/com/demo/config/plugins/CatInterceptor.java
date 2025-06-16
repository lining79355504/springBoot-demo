package com.demo.config.plugins;

import com.dianping.cat.CatConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mort
 * @Description
 * @date 2022/5/17
 **/
public class CatInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(CatConstants.CAT_PAGE_URI, getUrl(request));
        return true;
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
