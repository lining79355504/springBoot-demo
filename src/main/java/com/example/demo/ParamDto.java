package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serializable;

/**
 * @author mort
 * @Description
 * @date 2021/10/20
 **/

public class ParamDto implements Serializable {


    private static final long serialVersionUID = -3370148028386913386L;

    @JsonProperty(value = "biz_id")
    private String bizId;

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    @Override
    public String toString() {
        return "ParamDto{" +
                "bizId='" + bizId + '\'' +
                '}';
    }
}
