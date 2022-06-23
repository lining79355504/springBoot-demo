package com.demo.config;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * @author mort
 * @Description
 * @date 2022/5/18
 **/
public class ApplicationEventTest extends ApplicationEvent {

    public ApplicationEventTest(Object source) {
        super(source);
    }

    public ApplicationEventTest(Object source, Clock clock) {
        super(source, clock);
    }
}
