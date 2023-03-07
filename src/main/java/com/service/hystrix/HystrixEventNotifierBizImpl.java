package com.service.hystrix;

import com.dianping.cat.Cat;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixEventType;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.properties.HystrixDynamicPropertiesSystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author mort
 * @Description
 * @date 2023/3/6
 **/
@Component
public class HystrixEventNotifierBizImpl extends HystrixEventNotifier {

    private static final Logger logger = LoggerFactory.getLogger(HystrixEventNotifierBizImpl.class);

    private static final HystrixEventNotifier INSTANCE = new HystrixEventNotifierBizImpl();

    public HystrixEventNotifierBizImpl() {
    }

    public static HystrixEventNotifier getInstance() {
        return INSTANCE;
    }

    @Override
    public void markEvent(HystrixEventType eventType, HystrixCommandKey key) {
        logger.info(" {}, {}", eventType, key);
        if(eventType == HystrixEventType.TIMEOUT){
            String name = key.name(); // commandKey 默认方法名
        }else if(eventType ==  HystrixEventType.SUCCESS){   //正常无降级

        }else if(eventType ==  HystrixEventType.FALLBACK_SUCCESS){  //降级时间 降级一次执行一次
            Cat.logEvent("Hystrix", key.name());
        }
    }

    @Override
    public void markCommandExecution(HystrixCommandKey key, HystrixCommandProperties.ExecutionIsolationStrategy isolationStrategy, int duration, List<HystrixEventType> eventsDuringExecution) {
        logger.info("{} , {} , {}, {}", key, isolationStrategy, duration , eventsDuringExecution);
    }

    @PostConstruct
    public void initHystrixPlugin(){
        HystrixPlugins.getInstance().registerEventNotifier(HystrixEventNotifierBizImpl.getInstance());
    }
}
