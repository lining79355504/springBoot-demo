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
 * <p>
 * 打war包入口实现
 **/
@MapperScan("com.demo.dao")
@EnableSwagger2
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication(scanBasePackages = {"com.example", "com.service", "com.demo", "io.shardingsphere.transaction.aspect"})
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

    /*


warp

/Users/mort/.m2/repository/pleiades/component/rpc/rpc-client/1.0.37.RELEASE/rpc-client-1.0.37.RELEASE-sources.jar!/pleiades/component/rpc/client/BiliRpcClientCall.java:101   全链路trace原理 结合  opentelemetry

com.ailiaili.warp.spring.boot.autoconfigure.web.WarpHttpServerInterceptor


/Users/mort/.m2/repository/com/ailiaili/logback-otel-appender/1.2.6/logback-otel-appender-1.2.6-sources.jar!/com/ailiaili/logback/otel/appender/OpenTelemetryAppender.java:51


/Users/mort/.m2/repository/com/ailiaili/warp-spring-boot-autoconfigure/1.2.5.1/warp-spring-boot-autoconfigure-1.2.5.1-sources.jar!/com/ailiaili/warp/spring/boot/autoconfigure/web/HttpServerAutoConfiguration.java:48



/Users/mort/.m2/repository/org/springframework/spring-beans/5.3.14/spring-beans-5.3.14-sources.jar!/org/springframework/beans/factory/config/DependencyDescriptor.java:274


/Users/mort/.m2/repository/com/ailiaili/warp-spring-boot-autoconfigure/1.2.5.1/warp-spring-boot-autoconfigure-1.2.5.1-sources.jar!/com/ailiaili/warp/spring/boot/autoconfigure/web/WarpResponseBodyAdvice.java:47  http请求这里设置的traceId

/Users/mort/.m2/repository/com/ailiaili/warp-spring-boot-autoconfigure/1.2.5.1/warp-spring-boot-autoconfigure-1.2.5.1-sources.jar!/com/ailiaili/warp/spring/boot/autoconfigure/web/WarpResponseBodyAdvice.java:34



     war包方式

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class,
                        OpenTelemetryAutoConfiguration.class, // 最先加载
                        DiscoveryAutoConfiguration.class,
                        DatabusAutoConfiguration.class,
                        WarpGrpcAutoConfiguration.CommonComponentAutoConfiguration.class,
                        WarpGrpcAutoConfiguration.class,
                        EnvironmentVariableAutoConfiguration.class,
                        EmbeddedWebServerFactoryCustomizerAutoConfiguration.class,
                        HttpServerAutoConfiguration.class
                        )
                .properties("spring.main.allow-bean-definition-overriding=true")
                .properties("spring.main.allow-circular-references=true")
        ;
    }



        public static void main(String[] args) {
        try {
//            SpringApplication.run(Application.class, args);
            SpringApplication app = new SpringApplication(Application.class);
            // 手动指定配置类的顺序  指定内置jar里的bean启动顺序
            app.setSources(new LinkedHashSet<>(Arrays.asList(
                    OpenTelemetryAutoConfiguration.class.getName(), // 最先加载
                    DiscoveryAutoConfiguration.class.getName(),
                    DatabusAutoConfiguration.class.getName(),
                    WarpGrpcAutoConfiguration.CommonComponentAutoConfiguration.class.getName(),
                    WarpGrpcAutoConfiguration.class.getName(),
                    EnvironmentVariableAutoConfiguration.class.getName(),
                    EmbeddedWebServerFactoryCustomizerAutoConfiguration.class.getName(),
                    HttpServerAutoConfiguration.class.getName()
            )));
            app.run(args);

        }catch (Throwable throwable){
            throwable.printStackTrace();
            log.error("mgk-portal start error", throwable);
            System.exit(1);
        }
    }


     */
}
