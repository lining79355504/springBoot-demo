package com.demo.utils;

import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mort
 * @Description
 * @date 2023/5/17
 **/
@Component
public class ThreadPoolUtilsCatMonitor implements StatusExtension, Initializable {
    @Override
    public String getId() {
        return "ThreadPoolUtilsCatMonitor";
    }

    @Override
    public String getDescription() {
        return "thread pool monitor";
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> data = new HashMap<>();
        Executor singlePool = ThreadPoolUtils.getSinglePool();
        if(singlePool instanceof ThreadPoolExecutor){
            ThreadPoolExecutor t = (ThreadPoolExecutor) singlePool;
            data.put("Active", String.valueOf(t.getActiveCount()));
            data.put("Queued", String.valueOf(t.getQueue().size()));
            data.put("TEST", "100");
        }
        return data;
    }

    @PostConstruct
    @Override
    public void initialize() throws InitializationException {
        StatusExtensionRegister.getInstance().register(this);
    }
}
