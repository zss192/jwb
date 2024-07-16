package com.jwb.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/oauth")
@Api(value = "第三方认证接口", tags = "第三方认证接口")
public class RestAuthController {


    @Autowired
    private UserService userService;

    @ApiOperation("请求登录")
    @RequestMapping("/render/{source}")
    @ResponseBody
    public String renderAuth(@PathVariable("source") String source, HttpServletResponse response) throws IOException {
        log.info("进入render：" + source);
        // 根据平台名称获取授权请求工具类
        AuthRequest authRequest = userService.getAuthRequest(source);
        // 返回认证URL由前端进行跳转
        return authRequest.authorize(AuthStateUtils.createState());
    }

    @ApiOperation("扫码后回调")
    @RequestMapping("/callback/{source}")
    public String login(@PathVariable("source") String source, AuthCallback callback, HttpServletRequest request) {
        log.info("进入callback：" + source + " callback params：" + JSONObject.toJSONString(callback));
        AuthRequest authRequest = userService.getAuthRequest(source);
        // 根据第三方回调内容进行登录
        AuthResponse<AuthUser> response = authRequest.login(callback);
        log.info(JSONObject.toJSONString(response));

        if (response.ok()) {
            AuthUser user = response.getData();
            userService.save(user); // 存到Redis中
            // 在这里执行业务逻辑
            // 保存用户到数据库
            JwbUser jwbUser = userService.addUser(user);
            if (jwbUser == null) {
                return "redirect:http://localhost/error.html";
            }
            String username = user.getUuid();
            return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=user";

        }
        return "redirect:http://localhost/error.html";
    }
}
