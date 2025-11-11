package com.demo.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMetrics {
    
    private final ThreadPoolExecutor executor;
    private final String poolName;
    private final OpenTelemetry openTelemetry;
    
    public ThreadPoolMetrics(ThreadPoolExecutor executor, String poolName, OpenTelemetry openTelemetry) {
        this.executor = executor;
        this.poolName = poolName;
        this.openTelemetry = openTelemetry;
        this.registerMetrics();
    }
    
    private void registerMetrics() {
        Meter meter = openTelemetry
            .meterBuilder("thread.pool.metrics")
            .setInstrumentationVersion("1.0.0")
            .build();
            
        // 核心线程数
        meter.gaugeBuilder("thread.pool.core.size")
            .setDescription("Core thread pool size")
            .ofLongs()
            .buildWithCallback(measurement -> 
                measurement.record(executor.getCorePoolSize(), 
                    Attributes.of(AttributeKey.stringKey("pool"), poolName)));
        
        // 最大线程数
        meter.gaugeBuilder("thread.pool.max.size")
            .setDescription("Maximum thread pool size")
            .ofLongs()
            .buildWithCallback(measurement -> 
                measurement.record(executor.getMaximumPoolSize(), 
                    Attributes.of(AttributeKey.stringKey("pool"), poolName)));
                
        // 当前活跃线程数
        meter.gaugeBuilder("thread.pool.active.count")
            .setDescription("Active thread count")
            .ofLongs()
            .buildWithCallback(measurement -> 
                measurement.record(executor.getActiveCount(), 
                    Attributes.of(AttributeKey.stringKey("pool"), poolName)));
                
        // 队列大小
        meter.gaugeBuilder("thread.pool.queue.size")
            .setDescription("Task queue size")
            .ofLongs()
            .buildWithCallback(measurement -> 
                measurement.record(executor.getQueue().size(), 
                    Attributes.of(AttributeKey.stringKey("pool"), poolName)));
    }
}
