package com.jwb.company.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName jwb_user
 */
@TableName(value = "jwb_user")
@Data
@Getter
public class JwbUser implements Serializable {
    /**
     *
     */
    @TableId(value = "id")
    private String id;

    /**
     * 账户名或第三方id
     */
    @TableField(value = "username")
    private String username;

    /**
     *
     */
    @TableField(value = "password")
    private String password;

    /**
     *
     */
    @TableField(value = "salt")
    private String salt;

    /**
     * 微信unionid
     */
    @TableField(value = "third_unionid")
    private String third_unionid;

    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 头像
     */
    @TableField(value = "userpic")
    private String userpic;

    /**
     *
     */
    @TableField(value = "company_id")
    private String companyId;

    /**
     *
     */
    @TableField(value = "utype")
    private String utype;

    /**
     *
     */
    @TableField(value = "birthday")
    private LocalDateTime birthday;

    /**
     *
     */
    @TableField(value = "sex")
    private String sex;

    /**
     *
     */
    @TableField(value = "email")
    private String email;

    /**
     *
     */
    @TableField(value = "cellphone")
    private String cellphone;

    /**
     *
     */
    @TableField(value = "qq")
    private String qq;

    /**
     * 用户来源：注册还是第三方平台
     */
    @TableField(value = "source")
    private String source;

    /**
     * 用户状态
     */
    @TableField(value = "status")
    private String status;

    /**
     *
     */
    @TableField(value = "create_time")
    private LocalDateTime create_time;

    /**
     *
     */
    @TableField(value = "update_time")
    private LocalDateTime update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}