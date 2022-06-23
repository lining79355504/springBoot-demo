package com.demo.config;

import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author mort
 * @Description
 * @date 2022/5/18
 *
 * ApplicationEventPublisher  publisher 后这里监听
 **/
@Component
public class ApplicationEventListenerTest implements ApplicationListener<ApplicationEventTest>, Ordered {


    @Override
    public void onApplicationEvent(ApplicationEventTest event) {

        System.out.println("event = " + event);

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
