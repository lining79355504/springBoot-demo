package com.demo.local_snowflake;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class SnowflakeIdConfig {

    public static final String SNOWFLAKE_DATA_CENTER_ID = "cpm-component:snowflake_data_center_id";
//    @Resource
//    private RedissonClient redissonClient;
    //todo  mock
    private RedissonClient redissonClient ;



    private static final AtomicInteger DATA_CENTER_ID = new AtomicInteger(-1);

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() throws IllegalAccessException {
        for (int i = 0; i < 31; i++) {
            log.info("snowflakeIdWorker init trySet data_center_id {}", i);
            boolean trySet = redissonClient.getBucket(SNOWFLAKE_DATA_CENTER_ID + i).trySet(1, 15, TimeUnit.SECONDS);
            try {
                if (trySet) {
                    log.info("snowflakeIdWorker init success data_center_id {}", i);
                    DATA_CENTER_ID.set(i);
                    return new SnowflakeIdWorker(i);
                }
            } catch (Exception e) {
                log.error("snowflakeIdWorker init error", e);
                throw new IllegalAccessException("snowflakeIdWorker init error");
            }
        }
        if (DATA_CENTER_ID.get() < 0) {
            log.info("snowflakeIdWorker init error");
            throw new IllegalAccessException("snowflakeIdWorker init error");
        }
        return new SnowflakeIdWorker(DATA_CENTER_ID.get());
    }

    @PostConstruct
    public void heartLockBeat() {
        new Thread(() -> {
            while (true) {
                try {
                    boolean trySet = redissonClient.getBucket(SNOWFLAKE_DATA_CENTER_ID + DATA_CENTER_ID.get())
                            .trySet(1, 15, TimeUnit.SECONDS);
                    log.info("snowflakeIdWorker init heartLockBeat data_center_id {}",  DATA_CENTER_ID.get());
                    Thread.sleep(1000 * 5);
                } catch (Exception e) {
                    log.error("snowflakeIdWorker heartLockBeat error", e);
                }
            }
        }).start();
    }

    @PreDestroy
    public void destroy() {
        redissonClient.getBucket(SNOWFLAKE_DATA_CENTER_ID + DATA_CENTER_ID.get()).delete();
    }


}
