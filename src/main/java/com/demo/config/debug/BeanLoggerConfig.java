package com.demo.config.debug;

//import com.ailiali.discovery.DiscoveryClient;
import com.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class BeanLoggerConfig {
    @Bean
    public static BeanPostProcessor beanPostLogger() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof GreetingService) {
                    log.warn(">> GreetingService Bean '{}' created by definition in {}", beanName, bean.getClass().getName());
                    Map<String, Object> fieldMap = new LinkedHashMap<>();
                    for (Field field : bean.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        try {
                            fieldMap.put(field.getName(), field.get(bean));
                        } catch (IllegalAccessException e) {
                            log.warn(">> Field '{}' not accessible", field.getName());
                        }
                    }
                    log.warn(">> DiscoveryClientImpl field values: {}", fieldMap);
                }
                return bean;
            }
        };
    }
}

