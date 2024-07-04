package com.jwb.auth.controller;

import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.WxAuthService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Slf4j
@Controller
@Api(value = "微信登录接口", tags = "微信登录接口")
public class WxLoginController {
    @Autowired
    WxAuthService wxAuthService;

    // TODO：使用JustAuth支持更多第三方登录
    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        JwbUser xcUser = wxAuthService.wxAuth(code);
        if (xcUser == null) {
            return "redirect:http://localhost/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";
    }
}