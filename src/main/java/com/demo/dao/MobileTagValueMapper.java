package com.demo.dao;

import com.demo.entity.MobileTagValue;
import org.springframework.stereotype.Component;


public interface MobileTagValueMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mobile_tag_value
     *
     * @mbg.generated Wed Jul 10 19:12:21 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mobile_tag_value
     *
     * @mbg.generated Wed Jul 10 19:12:21 CST 2019
     */
    int insert(MobileTagValue record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mobile_tag_value
     *
     * @mbg.generated Wed Jul 10 19:12:21 CST 2019
     */
    int insertSelective(MobileTagValue record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mobile_tag_value
     *
     * @mbg.generated Wed Jul 10 19:12:21 CST 2019
     */
    MobileTagValue selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mobile_tag_value
     *
     * @mbg.generated Wed Jul 10 19:12:21 CST 2019
     */
    int updateByPrimaryKeySelective(MobileTagValue record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table mobile_tag_value
     *
     * @mbg.generated Wed Jul 10 19:12:21 CST 2019
     */
    int updateByPrimaryKey(MobileTagValue record);
}