package com.demo.config;

import com.dianping.cat.servlet.CatFilter;
import com.google.common.collect.Lists;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.contrib.sample.stream.HystrixUtilizationSseServlet;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Description 
 * @author mort
 * @date 2021/10/20
 **/
@Configuration
@EnableWebMvc
@PropertySource(value = {"classpath:application.properties"}, ignoreResourceNotFound = true)
public class WebConfig implements WebMvcConfigurer {

     private static final Logger log = LoggerFactory.getLogger(WebConfig.class);


    private static final List<String> VALUE = Lists.newArrayList("*.baidu.com", "*.baidu.co");

    private static final List<String> DEFAULT_VALUE = Lists.newArrayList("*");

    private String[] interceptorAntPatterns = {"/**"};

    @PostConstruct
    public void init() {
        log.info("======> WebConfig init ...");
    }

    /**
     * 等同于springMvc里的controller方法切面  <mvc:argument-resolvers>
     *
     *         <mvc:annotation-driven>
     *         <mvc:argument-resolvers>
     *             <bean class="com.demo.config.ContextWebArgumentResolverr"/>
     *         </mvc:argument-resolvers>
     *     </mvc:annotation-driven>
     *
     *
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        ContextWebArgumentResolver contextWebArgumentResolver = new ContextWebArgumentResolver();
        argumentResolvers.add(contextWebArgumentResolver);
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // equivalent to <mvc:message-converters>
    }

    //1：如果有 implements WebMvcConfigurer 的则如下addCorsMappings 重写方法不生效。
    // 2：则只能通过CorsFilter解决
    // 3：或者自己实现一个filter 设置responseHeader 支持跨域
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedHeaders("*")
//                .allowedMethods("GET","HEAD","POST","PUT","DELETE","OPTIONS")
////                .allowedOrigins("*")
//                .allowedOriginPatterns("*")
//                .exposedHeaders("access-control-allow-headers",
//                        "access-control-allow-methods",
//                        "access-control-allow-origin",
//                        "access-control-max-age",
//                        "X-Frame-Options")
//                .allowCredentials(true);
//    }

    //添加swagger-ui.html静态资源
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


    // 增加 cat interceptors
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new CatInterceptor())
                .addPathPatterns("/**");
    }

    private CorsConfiguration corsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedMethods(DEFAULT_VALUE);
//        corsConfiguration.setAllowedOrigins(VALUE);     //设置setAllowCredentials 只能用addAllowedOriginPattern  不然会报错
        corsConfiguration.setAllowedOriginPatterns(VALUE);
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig());
        return new CorsFilter(source);
    }

    /**
     * http://localhost:8080/spring_boot_demo_war_exploded/hystrix/utilization.stream
     * @return
     */
    @Bean
    public ServletRegistrationBean utilizationServletTLReportServlet() {
        return new ServletRegistrationBean(new HystrixUtilizationSseServlet(), "/hystrix/utilization.stream");
    }

    /**
     * http://localhost:8080/spring_boot_demo_war_exploded/hystrix/hystrix.stream
     * @return
     */
    @Bean
    public ServletRegistrationBean servletTLReportServlet() {
        return new ServletRegistrationBean(new HystrixMetricsStreamServlet(), "/hystrix/hystrix.stream");
    }


}
