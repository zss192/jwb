package com.jwb.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("jwb_user")
public class JwbUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String username;

    private String password;

    private String salt;

    private String nickname;
    private String thirdUnionid;
    private String companyId;
    /**
     * 头像
     */
    private String userpic;

    private String utype;

    private LocalDate birthday;

    private String sex;

    private String email;

    private String cellphone;

    private String qq;

    private String source;

    /**
     * 用户状态
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
