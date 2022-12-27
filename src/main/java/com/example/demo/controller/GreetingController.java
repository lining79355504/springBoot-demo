package com.example.demo.controller;

/**
 * Author:  lining17
 * Date :  2019-07-10
 */
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.demo.entity.Context;
import com.example.demo.GreetingDto;
import com.example.demo.ParamDto;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.service.GreetingService;
import com.service.es.AccTradingFlowDayPo;
import com.service.es.EsOpService;
import com.service.hystrix.CommandHelloFailure;
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

    @Autowired
    private EsOpService esOpService;

    @ApiOperation(value = "greeting 方法")
    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    public GreetingDto greeting(@ApiIgnore Context context, ParamDto paramDto) {
//        MobileTagValue mobileTagValue = greetingService.getById(36926);
//        return new GreetingDto(counter.incrementAndGet(),
//                String.format(template, mobileTagValue.toString()), "test");
        logger.info("paramDto  {}", paramDto);

        List<AccTradingFlowDayPo> pos = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            pos.add(AccTradingFlowDayPo.builder()
                    .id(String.valueOf(UUID.randomUUID().hashCode()))
                    .cash_balance(0L)
                    .account_id(1)
                    .ctime(new Timestamp(System.currentTimeMillis()))
                    .mtime(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        esOpService.bulk("acc_account_trading_flow_day_v1", pos);

        esOpService.scrollQuery(AccTradingFlowDayPo.class, "acc_account_trading_flow_day_v1" , 100, 2);

        List<Map<String, Object>> maps = jdbcTemplate.queryForList("select * from alert limit 10");
        return new GreetingDto(counter.incrementAndGet(),
                "content", "test");
    }


    @ApiOperation(value = "greeting 方法")
    @RequestMapping(value = "/cricuit_test", method = RequestMethod.GET)
    public String circuitTest(@ApiIgnore Context context, ParamDto paramDto) {
        return new CommandHelloFailure(HystrixCommandGroupKey.Factory.asKey("ExampleGroup")).execute();
    }


    @RequestMapping(value = "/change_cricuit_value", method = RequestMethod.GET)
    public String changeCircuitValue(@RequestParam("time") Integer value) {
        new CommandHelloFailure(HystrixCommandGroupKey.Factory.asKey("ExampleGroup")).changeRunTest(value);
        return "success";
    }

}