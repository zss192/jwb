package com.jwb.content.model.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;

/**
 * 课程分类(CourseCategory)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
public class CourseCategory implements Serializable {
    private static final long serialVersionUID = 431765689658304013L;
    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类标签默认和名称一样
     */
    private String label;
    /**
     * 父结点id（第一级的父节点是0，自关联字段id）
     */
    private String parentid;
    /**
     * 是否显示
     */
    private Integer isShow;
    /**
     * 排序字段
     */
    private Integer orderby;
    /**
     * 是否叶子
     */
    private Integer isLeaf;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    public Integer getOrderby() {
        return orderby;
    }

    public void setOrderby(Integer orderby) {
        this.orderby = orderby;
    }

    public Integer getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Integer isLeaf) {
        this.isLeaf = isLeaf;
    }

}

