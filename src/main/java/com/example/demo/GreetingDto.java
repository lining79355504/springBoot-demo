package com.example.demo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serializable;

/**
 * Author:  lining17
 * Date :  2019-07-10
 *
 * jackson
 *
 * @JsonUnwrapped  扁平化对象 会把对象内部的field抽到外层里  （抽出嵌套）
 *
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GreetingDto implements Serializable {


    private static final long serialVersionUID = 5472045612167347866L;

    private long id;

    private String content;

    private String paramTest;

    public GreetingDto(long id, String content, String paramTest) {
        this.id = id;
        this.content = content;
        this.paramTest = paramTest;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getParamTest() {
        return paramTest;
    }

    public void setParamTest(String paramTest) {
        this.paramTest = paramTest;
    }

    @Override
    public String toString() {
        return "GreetingDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", paramTest='" + paramTest + '\'' +
                '}';
    }
}