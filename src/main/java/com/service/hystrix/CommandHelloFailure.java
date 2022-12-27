package com.service.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mort
 * @Description
 * @date 2022/12/27
 **/

public class CommandHelloFailure extends HystrixCommand<String> {

    private static volatile AtomicInteger RUN_TEST = new AtomicInteger(5);

    protected CommandHelloFailure(HystrixCommandGroupKey group, HystrixThreadPoolKey threadPool, int executionIsolationThreadTimeoutInMilliseconds) {
        super(group, threadPool, executionIsolationThreadTimeoutInMilliseconds);
    }

    public CommandHelloFailure(HystrixCommandGroupKey group) {
//        HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("ExampleGroup");
        super(Setter.withGroupKey(group)
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                                .withExecutionTimeoutInMilliseconds(10)
//                        .withCircuitBreakerEnabled()
//                        .withCircuitBreakerErrorThresholdPercentage()
//                        .withCircuitBreakerForceClosed()
//                        .withCircuitBreakerForceOpen()
//                        .withCircuitBreakerRequestVolumeThreshold()
//                        .withExecutionIsolationStrategy()
//                        .withFallbackEnabled()
                ));
    }

//    public CommandHelloFailure(String commandGroupKey) {
//        HystrixCommandGroupKey hystrixCommandGroup = HystrixCommandGroupKey.Factory.asKey(commandGroupKey);
//        super(hystrixCommandGroup);
//    }

    @Override
    protected String run() throws Exception {
        Thread.sleep(RUN_TEST.get());
        return "success";
    }

    @Override
    protected String getFallback() {
        return "i am fall back";
    }

    public void changeRunTest(Integer value) {
        RUN_TEST.set(value);
    }
}
