package com.demo.local_snowflake;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.RScript;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class SnowflakeIdConfig {

    public static final String SNOWFLAKE_DATA_CENTER_ID = "cpm-component:snowflake_data_center_id";

    //mock
    private RedissonClient redissonClient;

    private static final int MAX_DATA_CENTER_ID = 31;
    private static final long LEASE_SECONDS = 15L;
    private static final long RENEW_INTERVAL_SECONDS = 5L;
    private static final long LEASE_SAFETY_MARGIN_SECONDS = 5L;
    private static final String RENEW_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end";
    private static final String RELEASE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "return redis.call('del', KEYS[1]) else return 0 end";

    private final String instanceToken = UUID.randomUUID().toString();
    private final AtomicInteger dataCenterId = new AtomicInteger(-1);
    private final ScheduledExecutorService leaseScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "snowflake-lease-renewer");
        thread.setDaemon(true);
        return thread;
    });

    private volatile String leaseKey;
    private volatile SnowflakeIdWorker worker;
    private volatile boolean leaseHeld;

//    /**
//     * RedissonClient 可以由生产 Redis 客户端或测试中的 mock 实现提供。
//     */
//    public SnowflakeIdConfig(RedissonClient redissonClient) {
//        this.redissonClient = redissonClient;
//    }

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() throws IllegalAccessException {
        for (int i = 0; i <= MAX_DATA_CENTER_ID; i++) {
            try {
                log.info("snowflakeIdWorker init trySet data_center_id {}", i);
                boolean trySet = redissonClient.getBucket(SNOWFLAKE_DATA_CENTER_ID + i)
                        .trySet(instanceToken, LEASE_SECONDS, TimeUnit.SECONDS);
                if (trySet) {
                    log.info("snowflakeIdWorker init success data_center_id {}", i);
                    dataCenterId.set(i);
                    leaseKey = SNOWFLAKE_DATA_CENTER_ID + i;
                    leaseHeld = true;
                    worker = new SnowflakeIdWorker(i);
                    updateWorkerLeaseDeadline();
                    startLeaseRenewal();
                    return worker;
                }
            } catch (Exception e) {
                releaseLease();
                log.error("snowflakeIdWorker init error", e);
                throw new IllegalAccessException("snowflakeIdWorker init error");
            }
        }
        throw new IllegalAccessException("No available snowflake data center ID");
    }

    private void startLeaseRenewal() {
        leaseScheduler.scheduleAtFixedRate(this::renewLease, RENEW_INTERVAL_SECONDS,
                RENEW_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void renewLease() {
        if (!leaseHeld) {
            return;
        }
        try {
            Long renewed = redissonClient.getScript().eval(
                    RScript.Mode.READ_WRITE,
                    RENEW_SCRIPT,
                    RScript.ReturnType.INTEGER,
                    Collections.singletonList(leaseKey),
                    instanceToken,
                    TimeUnit.SECONDS.toMillis(LEASE_SECONDS));
            if (renewed == null || renewed != 1L) {
                loseLease("lease is no longer owned by this instance", null);
            } else {
                updateWorkerLeaseDeadline();
            }
        } catch (Exception e) {
            loseLease("could not renew lease", e);
        }
    }

    private void loseLease(String reason, Exception cause) {
        if (!leaseHeld) {
            return;
        }
        leaseHeld = false;
        if (worker != null) {
            worker.markUnavailable();
        }
        if (cause == null) {
            log.error("Snowflake lease lost for dataCenterId={}: {}; ID generation has been disabled",
                    dataCenterId.get(), reason);
        } else {
            log.error("Snowflake lease lost for dataCenterId={}: {}; ID generation has been disabled",
                    dataCenterId.get(), reason, cause);
        }
    }

    private void updateWorkerLeaseDeadline() {
        long safeLeaseMillis = TimeUnit.SECONDS.toMillis(LEASE_SECONDS - LEASE_SAFETY_MARGIN_SECONDS);
        worker.updateLeaseDeadline(System.currentTimeMillis() + safeLeaseMillis);
    }

    @PreDestroy
    public void destroy() {
        leaseScheduler.shutdownNow();
        releaseLease();
    }

    private void releaseLease() {
        if (!leaseHeld || leaseKey == null) {
            return;
        }
        try {
            redissonClient.getScript().eval(
                    RScript.Mode.READ_WRITE,
                    RELEASE_SCRIPT,
                    RScript.ReturnType.INTEGER,
                    Collections.singletonList(leaseKey),
                    instanceToken);
        } catch (Exception e) {
            log.warn("Failed to release Snowflake lease for dataCenterId={}", dataCenterId.get(), e);
        } finally {
            leaseHeld = false;
        }
    }

}
