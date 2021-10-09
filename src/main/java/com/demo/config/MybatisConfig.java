package com.demo.config;

import com.demo.config.mybatis.plugins.ShowSql;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * @author mort
 * @Description
 * @date 2021/10/9
 **/
@org.springframework.context.annotation.Configuration
public class MybatisConfig {

    //将插件加入到mybatis插件拦截链中
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(Configuration configuration) {
                //插件拦截链采用了责任链模式，执行顺序和加入连接链的顺序有关
                ShowSql myPlugin = new ShowSql();
                configuration.addInterceptor(myPlugin);
            }
        };
    }

}
