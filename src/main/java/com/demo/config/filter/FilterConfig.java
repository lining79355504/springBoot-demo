package com.demo.config.filter;

import com.dianping.cat.servlet.CatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mort
 * @Description
 * @date 2022/7/12
 **/
@Configuration
public class FilterConfig {

    @Value("${value:asas}")
    private String value;

    @Value("#{'${id:1,2,3}'.split(',')}")
    private List<Integer> ids;

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

    @Bean
    public FilterRegistrationBean registerCatFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CatFilter());
        registration.addUrlPatterns("/*");
        registration.setName("catFilter");
        registration.setOrder(2);  //值越小，Filter越靠前。
        return registration;
    }

    /**
     * 创建一个bean
     *
     * @return
     */
    @Bean(name = "authFilterBean")
    public AuthFilter authFilter() {
        return new AuthFilter();
    }

}

