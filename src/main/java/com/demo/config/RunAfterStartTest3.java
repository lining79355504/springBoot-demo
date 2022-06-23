package com.demo.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author mort
 * @Description
 * @date 2022/5/18
 * <p>
 * SpringBoot 启动时自动执行代码的几种方式
 * <p>
 * Spring应用启动过程中，肯定是要自动扫描有@Component注解的类，加载类并初始化对象进行自动注入。加载类时首先要执行static静态代码块中的代码，之后再初始化对象时会执行构造方法。
 * 在对象注入完成后，调用带有@PostConstruct注解的方法。当容器启动成功后，再根据@Order注解的顺序调用CommandLineRunner和ApplicationRunner接口类中的run方法。
 * 因此，加载顺序为static>constructer>@PostConstruct>CommandLineRunner和ApplicationRunner
 * @See JobLauncherApplicationRunner
 **/
@Component
public class RunAfterStartTest3 implements ApplicationRunner, Ordered, ApplicationEventPublisherAware {


    private ApplicationEventPublisher publisher;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Object object = new Object();
        ApplicationEventTest event = new ApplicationEventTest(object);
        publisher.publishEvent(event);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
