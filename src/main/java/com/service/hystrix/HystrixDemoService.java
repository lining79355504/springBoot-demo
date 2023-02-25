package com.service.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
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
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10")

            }
    )
    public String executeTest(String name){
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "run"+name;
    }

    public String executeTestFallBack(String name){
        return "fail"+name;
    }
}
