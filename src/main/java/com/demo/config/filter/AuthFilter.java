package com.demo.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author mort
 * @Description
 * @date 2022/7/12
 **/

@Component
public class AuthFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private static String value = "";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        value = filterConfig.getInitParameter("value");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("start to auth request");
        log.info(value);
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("token");
        if (token != null) {
            //    :TODO check token
            log.info("auth success");
            chain.doFilter(request, response);
        } else {
            log.error("auth failed");
        }
    }
}
