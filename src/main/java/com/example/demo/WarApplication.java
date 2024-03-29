package com.example.demo;

import com.google.common.collect.Lists;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

/**
 * @author mort
 * @Description
 * @date 2021/9/28
 *
 * 打war包入口实现
 *
 **/
@MapperScan("com.demo.dao")
@EnableSwagger2
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication(scanBasePackages = {"com.example","com.service", "com.demo", "io.shardingsphere.transaction.aspect"})
public class WarApplication extends SpringBootServletInitializer {

    //war包方式
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WarApplication.class);
    }

    // 原始spring boot 类启动
    public static void main(String[] args) {
        List<List<Integer>> parts = Lists.partition(Lists.newArrayList(1, 2, 3), 200);
        SpringApplication.run(WarApplication.class, args);
    }
}
