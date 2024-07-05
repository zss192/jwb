package com.jwb.ucenter.model.dto;

import com.jwb.ucenter.model.po.JwbUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JwbUserExt extends JwbUser {
    //用户权限
    List<String> permissions = new ArrayList<>();
}
