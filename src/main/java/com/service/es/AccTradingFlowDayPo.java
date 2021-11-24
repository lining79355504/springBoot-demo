package com.service.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.sql.Timestamp;

/**
 * @author mort
 * @Description
 * @date 2021/8/10
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "acc_account_trading_flow_day_v1", shards = 3, indexStoreType = "base")
//1 spring data es 如果没有索引 第一次会新建索引
// 7.0 后移除了type  之前会默认给一个type 为base的类型
public class AccTradingFlowDayPo {

    private Long cash_recharge;
    private Long red_packet_cheat;
    @Field(type = FieldType.Keyword)
    private Integer department_id;
    private Long red_packet_balance;
    private Long cash_cheat;
    private Long red_packet_recharge;
    private Long red_packet_turn_in;
    private Long cash_turn_in;
    private Long cash_turn_out;
    private Long cash_consume;
    private Long special_red_packet_consume;
    private Long cash_transfer_in;

    @Field(type = FieldType.Keyword)
    private Integer agent_id;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp mtime;
    @Field(type = FieldType.Keyword)
    private Integer is_deleted;
    @Field(type = FieldType.Keyword)
    private Integer is_agent;
    private Long cash_transfer_out;
    private Long red_packet_refund;

    @Id  //主键必须为string es里对应 _id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")  //分词示例
    private String title;

    @Field(type = FieldType.Double)
    private Double price; // 价格

    private Long special_red_packet_balance;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp date;
    private Long special_red_packet_recharge;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp ctime;
    private Long cash_balance;
    private Long red_packet_consume;
    private Long special_red_packet_refund;
    private Long red_packet_turn_out;
    @Field(type = FieldType.Keyword)
    private Integer account_id;
    private Long cash_refund;
    private Long red_packet_transfer_in;
    private Long red_packet_transfer_out;
}
