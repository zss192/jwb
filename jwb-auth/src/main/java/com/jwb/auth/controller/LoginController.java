package com.jwb.auth.controller;

import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.po.JwbUser;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(value = "认证服务接口", tags = "认证服务接口")
public class LoginController {

    @Autowired
    JwbUserMapper jwbUserMapper;


    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public JwbUser getuser(@PathVariable("id") String id) {
        return jwbUserMapper.selectById(id);
    }

    @RequestMapping("/r/r1")
    @PreAuthorize("hasAnyAuthority('p1')")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    @PreAuthorize("hasAuthority('p2')")
    public String r2() {
        return "访问r2资源";
    }


}
