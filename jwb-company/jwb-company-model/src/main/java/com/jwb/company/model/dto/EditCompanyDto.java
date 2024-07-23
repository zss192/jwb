package com.jwb.company.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class EditCompanyDto {
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
     * 联系方式
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
     * 地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * logo
     */
    @TableField(value = "logo")
    private String logo;
}
