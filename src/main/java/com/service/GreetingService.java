package com.service;

import com.demo.dao.MobileTagValueMapper;
import com.demo.entity.MobileTagValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author:  lining17
 * Date :  2019-07-10
 */
@Component
public class GreetingService {

    @Autowired
    private MobileTagValueMapper mobileTagValueMapper;

    public MobileTagValue getById(Integer id) {
        return mobileTagValueMapper.selectByPrimaryKey(id);
    }


}
