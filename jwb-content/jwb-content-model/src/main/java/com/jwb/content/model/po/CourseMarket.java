package com.jwb.content.model.po;

import java.io.Serializable;

/**
 * 课程营销信息(CourseMarket)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
public class CourseMarket implements Serializable {
    private static final long serialVersionUID = 526543025485162597L;
    /**
     * 主键，课程id
     */
    private Long id;
    /**
     * 收费规则，对应数据字典
     */
    private String charge;
    /**
     * 现价
     */
    private Object price;
    /**
     * 原价
     */
    private Object originalPrice;
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public Object getPrice() {
        return price;
    }

    public void setPrice(Object price) {
        this.price = price;
    }

    public Object getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Object originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getValidDays() {
        return validDays;
    }

    public void setValidDays(Integer validDays) {
        this.validDays = validDays;
    }

}

