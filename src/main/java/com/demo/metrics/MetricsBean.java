package com.demo.metrics;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MetricsBean {
    @PostConstruct
    private void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        log.info("{}: initialized", "MetricsBean");
        //每5s上报一次
        scheduler.scheduleAtFixedRate(() -> {
                    log.info("{}: threadPool 上报", "MetricsBean");
//                    executorMap.forEach((name, executor) -> {
//                        if (executor.isShutdown()) {
//                            log.warn("{}: executor {} is shutdown, will not collect metrics", ID, name);
//                            return;
//                        }
//                        new ThreadPoolMetrics(executor, name, openTelemetry);
//                    });

                },
                5,
                5, // 延迟5秒
                TimeUnit.SECONDS);
    }
}
