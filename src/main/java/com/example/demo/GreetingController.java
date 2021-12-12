package com.example.demo;

/**
 * Author:  lining17
 * Date :  2019-07-10
 */
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.demo.entity.Context;
import com.demo.entity.MobileTagValue;
import com.service.GreetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "swagger doc controller test", description = "desc")
@RestController
public class GreetingController {

     private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    private static final String template = "Hello, %s!";

    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GreetingService greetingService;

    @ApiOperation(value = "greeting 方法")
    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    public GreetingDto greeting(@ApiIgnore Context context, ParamDto paramDto) {
//        MobileTagValue mobileTagValue = greetingService.getById(36926);
//        return new GreetingDto(counter.incrementAndGet(),
//                String.format(template, mobileTagValue.toString()), "test");
        logger.info("paramDto  {}", paramDto);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from alert limit 10");
        return new GreetingDto(counter.incrementAndGet(),
                "content", "test");
    }
}