package com.demo.config;

import com.google.gson.Gson;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author mort
 * @Description
 * @date 2022/5/18
 * <p>
 * SpringBoot 启动时自动执行代码的几种方式
 * <p>
 * Spring应用启动过程中，肯定是要自动扫描有@Component注解的类，加载类并初始化对象进行自动注入。加载类时首先要执行static静态代码块中的代码，之后再初始化对象时会执行构造方法。
 * 在对象注入完成后，调用带有@PostConstruct注解的方法。当容器启动成功后，再根据@Order注解的顺序调用CommandLineRunner和ApplicationRunner接口类中的run方法。
 * 因此，加载顺序为static>constructer>@PostConstruct>CommandLineRunner和ApplicationRunner
 **/
@Component
public class RunAfterStartTest2 implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        String argsStr = new Gson().toJson(args);
        System.out.println("CommandLineRunner args = " + Arrays.deepToString(args));
    }
}
