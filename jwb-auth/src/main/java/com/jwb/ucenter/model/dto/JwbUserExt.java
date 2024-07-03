package com.jwb.ucenter.model.dto;

import com.jwb.ucenter.model.po.JwbUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 用户扩展信息
 * @date 2022/9/30 13:56
 */
@Data
public class JwbUserExt extends JwbUser {
    //用户权限
    List<String> permissions = new ArrayList<>();
}
