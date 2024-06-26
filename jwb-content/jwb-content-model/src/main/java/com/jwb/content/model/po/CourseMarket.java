package com.jwb.content.model.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 课程营销信息(CourseMarket)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
@Data
public class CourseMarket implements Serializable {
    private static final long serialVersionUID = 526543025485162597L;
    /**
     * 主键，课程id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 收费规则，对应数据字典
     */
    private String charge;
    /**
     * 现价
     */
    private Float price;
    /**
     * 原价
     */
    private Float originalPrice;
    /**
     * 咨询qq
     */
    private String qq;
    /**
     * 微信
     */
    private String wechat;
    /**
     * 电话
     */
    private String phone;
    /**
     * 有效期天数
     */
    private Integer validDays;


}

