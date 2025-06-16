package com.example.demo.controller;

/**
 * Author:  lining17
 * Date :  2019-07-10
 */
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.demo.entity.Context;
import com.example.demo.GreetingDto;
import com.example.demo.ParamDto;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.service.GreetingService;
import com.service.es.AccTradingFlowDayPo;
import com.service.es.EsOpService;
import com.service.hystrix.CommandHelloFailure;
import com.service.hystrix.HystrixDemoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    //  @since 9  才有这种初始化方式
//    private static Map<Integer, String> sortMap = Map.of(
//            1,"2",
//            2,"3",
//            3,"4",
//            4,"5"
//    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private EsOpService esOpService;

    @Autowired
    private HystrixDemoService hystrixDemoService;

    /*


    aigc.sensitive.keywords.map ={"封面图片低俗":{"身体特写：半乳、側乳特写","身体特写：锁骨（审核侧目前不再卡锁骨 故删去）","身体特写：乳沟","身体特写：胸部凸点或露点","身体特写：胸部遮挡","身体特写：圣涡、背部裸露","身体特写：胸部","身体特写：大腿","视觉聚焦：角度视觉聚焦-胸部","视觉聚焦：角度视觉聚焦-腿部","视觉聚焦：敏感部位故意遮挡导致聚焦-胸部","视觉聚焦：敏感部位故意遮挡导致聚焦-腿部","着装规范：半透明、镂空衣服","着装规范：湿身、半透明","着装规范：内衣","着装规范：短裙、短裤","着装规范：黑白丝","动作不雅：挤胸、托胸","动作不雅：翘臀、跪趴、叉腿","词汇：不良词汇","词汇：低俗词汇、低俗联想","词汇：法律禁止","词汇：低俗癖好","标题文案低俗"},              "封面视频低俗":{"身体特写：半乳、側乳特写","身体特写：锁骨（审核侧目前不再卡锁骨 故删去）","身体特写：乳沟","身体特写：胸部凸点或露点","身体特写：胸部遮挡","身体特写：圣涡、背部裸露","身体特写：胸部","身体特写：大腿","视觉聚焦：角度视觉聚焦-胸部","视觉聚焦：角度视觉聚焦-腿部","视觉聚焦：敏感部位故意遮挡导致聚焦-胸部","视觉聚焦：敏感部位故意遮挡导致聚焦-腿部","着装规范：半透明、镂空衣服","着装规范：湿身、半透明","着装规范：内衣","着装规范：短裙、短裤","着装规范：黑白丝","动作不雅：挤胸、托胸","动作不雅：翘臀、跪趴、叉腿","词汇：不良词汇","词汇：低俗词汇、低俗联想","词汇：法律禁止","词汇：低俗癖好","标题文案低俗"},                                                 "文案低俗-标题&描述":{"标题低俗","词汇：不良词汇"},          "商业/带货起飞":{"绝对化用语", "低俗-文案", "不文明用语"},        "广告法 、平台相关":{"常用内容：最高级、绝对化用语"}}

    spel 表达式 高级用法
    @Value("#{${aigc.sensitive.keywords.map}}")
    private Map<String, Set<String>> AIGC_SENSITIVE_KEYWORDS_MAP;


     */


    @ApiOperation(value = "greeting 方法")
    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    public GreetingDto greeting(@ApiIgnore Context context, ParamDto paramDto) {
//        MobileTagValue mobileTagValue = greetingService.getById(36926);
//        return new GreetingDto(counter.incrementAndGet(),
//                String.format(template, mobileTagValue.toString()), "test");

        String str = "operator {\n" +
                "  operator_id: 94002939\n" +
                "  operator_name: \"\\344\\270\\211\\350\\277\\236\\351\\207\\215\\346\\236\\204-\\345\\271\\277\\345\\221\\212\\344\\270\\273\"\n" +
                "  operator_type: 100\n" +
                "  system_type: 2\n" +
                "   user_name: \"lining02\"\n" +
                "}\n" +
                "unit {\n" +
                "  campaign_id: 1424170\n" +
                "  unit_name: \"ln-app-download\"\n" +
                "  promotion_content_type: PPC_APP_DOWNLOAD\n" +
                "  launch_begin_date: \"2024-08-02\"\n" +
                "  launch_time: \"111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\"\n" +
                "  budget: \"300\"\n" +
                "  sales_mode {\n" +
                "    base_target: BT_CPM\n" +
                "    base_bid: \"0\"\n" +
                "    cpa_target: CPA_APP_ACTIVE\n" +
                "    cpa_bid: \"20\"\n" +
                "  }\n" +
                "  speed_mode: SPEED_NORMAL\n" +
                "  app_package {\n" +
                "    app_package_id: 54620\n" +
                "  }\n" +
                "  game {\n" +
                "  }\n" +
                "  live {\n" +
                "  }\n" +
                "  goods {\n" +
                "  }\n" +
                "  live_reserve {\n" +
                "  }\n" +
                "  target {\n" +
                "    os {\n" +
                "      os_target: OS_IOS\n" +
                "      os_bili_client {\n" +
                "      }\n" +
                "    }\n" +
                "    os {\n" +
                "      os_target: OS_IPAD\n" +
                "      os_bili_client {\n" +
                "      }\n" +
                "    }\n" +
                "    crowd_pack {\n" +
                "    }\n" +
                "    arc_tag_interest {\n" +
                "      tag_fuzzy_type: EXACT\n" +
                "    }\n" +
                "    arc_tag {\n" +
                "      tag_fuzzy_type: EXACT\n" +
                "    }\n" +
                "    installed_user_filter {\n" +
                "    }\n" +
                "    intelligent_mass {\n" +
                "    }\n" +
                "    archive_content {\n" +
                "    }\n" +
                "  }\n" +
                "  smart_key_word: 1\n" +
                "}";
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


    @ApiOperation(value = "demo 方法")
    @RequestMapping(value = "/demo", method = RequestMethod.GET)
    public String demo(@ApiIgnore Context context, ParamDto paramDto) {
        return hystrixDemoService.executeTest("mort");
    }



}