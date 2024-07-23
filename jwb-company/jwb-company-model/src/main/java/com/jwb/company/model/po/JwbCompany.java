package com.jwb.company.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


@TableName(value = "jwb_company")
@Data
public class JwbCompany implements Serializable {
    /**
     *
     */
    @TableId(value = "id")
    private String id;

    /**
     * 联系人名称
     */
    @TableField(value = "linkname")
    private String linkname;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     *
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     *
     */
    @TableField(value = "email")
    private String email;

    /**
     * 简介
     */
    @TableField(value = "intro")
    private String intro;

    /**
     * 简介
     */
    @TableField(value = "address")
    private String address;

    /**
     * logo
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 身份证照片
     */
    @TableField(value = "identitypic")
    private String identitypic;

    /**
     * 工具性质
     */
    @TableField(value = "worktype")
    private String worktype;

    /**
     * 营业执照
     */
    @TableField(value = "businesspic")
    private String businesspic;

    /**
     * 企业状态
     */
    @TableField(value = "status")
    private String status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}