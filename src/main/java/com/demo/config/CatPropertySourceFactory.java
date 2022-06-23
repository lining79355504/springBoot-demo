package com.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author mort
 * @Description
 * @date 2022/5/11
 **/

/**
 * 自动定义  PropertySourceFactory
 *
 * 用法   @PropertySource(value = "classpath:cat.xml",  factory = CatPropertySourceFactory.class)
 *
 *  @PropertySource(value = "classpath:dats_source.properties",  factory = CatPropertySourceFactory.class)
 *  配置文件的注解上使用 指定解析该文件的PropertySourceFactory
 *
 *  使用 @PropertySource 注解，可以编写配置文件的映射类，取代 @Value，在需要用到的地方，用 @Autowire 自动装配配置类即可
 *
 * @Value动态刷新的问题的问题，springboot中使用@RefreshScope实现了。
 *
 *  simple
 *
 *  @Configuration
 *  @PropertySource(value = "classpath:job-manager.properties",  factory = PaladinPropertySourceFactory.class)
 *  @RefreshScope
 * public class JobConfig {
 *     @Value("${xxl.job.admin.addresses}")
 *     private String address;
 *     @Value("${xxl.job.executor.appname:test}")
 *     private String appname;
 *     @Value("${xxl.job.executor.ip:127.0.0.1}")
 *        private String ip;
 *
 *
 *     // 带默认值用法
 *     @Value("#{'${departments:1,110,8,176,175}'.split(',')}")
 *     private List<Integer> list;
 *     }
 *
 *
 */
public class CatPropertySourceFactory extends DefaultPropertySourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(CatPropertySourceFactory.class);

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = name != null ? name : resource.getResource().getFilename();
        if (sourceName.equals("cat.xml")) {
//            ConfigSource configSource = ConfigSourceHolder.get();
//            ConfigEntry configEntry = configSource.getNullableEntry("cat").get();
//            String rawValue = configEntry.getRawValue();
//            writeFile(rawValue);
        }
        return super.createPropertySource(name, resource);
    }


    public void writeFile(String content) {
        try {

            String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            File file = new File(path + "/cat.xml");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWritter = new FileWriter(file.getPath(), false);
            fileWritter.write(content);
            fileWritter.close();

        } catch (IOException e) {
            logger.error("write cat client xml error ", e);
        }
    }
}
