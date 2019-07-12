package com.example.demo;

/**
 * Author:  lining17
 * Date :  2019-07-10
 */
import java.util.concurrent.atomic.AtomicLong;

import com.demo.entity.MobileTagValue;
import com.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private GreetingService greetingService;

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        MobileTagValue mobileTagValue = greetingService.getById(36926);
        return new Greeting(counter.incrementAndGet(),
                String.format(template, mobileTagValue.toString()));
    }
}