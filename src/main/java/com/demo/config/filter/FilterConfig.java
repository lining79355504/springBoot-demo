package com.demo.config.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mort
 * @Description
 * @date 2022/7/12
 **/
@Configuration
public class FilterConfig {

    @Value("${value}")
    private String value;

    @Bean
    public FilterRegistrationBean registerAuthFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authFilter());
        registration.addUrlPatterns("/*");
        registration.setName("authFilter");
        registration.setOrder(1);  //值越小，Filter越靠前。
        // 传入参数
        Map<String, String> initParameters = new HashMap<String, String>();
        initParameters.put("value", value);
        registration.setInitParameters(initParameters);
        return registration;
    }

    //如果有多个Filter，再写一个public FilterRegistrationBean registerOtherFilter(){...}即可。


    /**
     * 创建一个bean
     *
     * @return
     */
    @Bean(name = "authFilter")
    public AuthFilter authFilter() {
        return new AuthFilter();
    }

}

