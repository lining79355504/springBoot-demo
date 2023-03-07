package com.service.hystrix;

import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifierDefault;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherDefault;
import org.springframework.stereotype.Service;

/**
 * @author mort
 * @Description
 * @date 2023/2/25
 **/

/**
 * 引入         <dependency>
 *             <groupId>com.netflix.hystrix</groupId>
 *             <artifactId>hystrix-javanica</artifactId>
 *             <version>${hystrix-version}</version>
 *         </dependency>
 *
 *         引入 com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect 却面
 *
 *          @EnableAspectJAutoProxy
 * @Configuration
 * public class HystrixConfig {
 *
 *     @Bean
 *     public HystrixCommandAspect hystrixAspect() {
 *         return new HystrixCommandAspect();
 *     }
 *
 *
 * }
 *
 *         支持  @HystrixCommand 注解使用
 *
 *         javanica适用于JDK和CGLIB代理
 *
 *        @HystrixCommand  配置参考 HystrixPropertiesManager
 */
@Service
public class HystrixDemoService {

    @HystrixCommand(
            fallbackMethod = "executeTestFallBack",
            ignoreExceptions = {Throwable.class},  //忽略异常 只有超时才会降级
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10"),
                    @HystrixProperty(name = "execution.isolation.semaphore.maxConcurrentRequests", value = "1"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"), // 触发熔断 降级后 多久试一次原方法恢复没
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50") // 失败比例占到多少value%后 触发熔断降级

            }
    )
    public String executeTest(String name){
        try {

//            HystrixServoMetricsPublisher.getInstance().getMetricsPublisherForCommand()
            //模拟超时
//            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        HystrixEventNotifierDefault.getInstance()

        HystrixMetricsPublisher instance = HystrixMetricsPublisherDefault.getInstance();





        return "run"+name;
    }

    public String executeTestFallBack(String name){
        return "fail"+name;
    }
}
