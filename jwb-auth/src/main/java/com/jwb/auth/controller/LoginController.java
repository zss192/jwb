package com.jwb.auth.controller;

import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.dto.FindPswDto;
import com.jwb.ucenter.model.dto.RegisterDto;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.VerifyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(value = "认证服务接口", tags = "认证服务接口")
public class LoginController {

    @Autowired
    JwbUserMapper jwbUserMapper;
    @Autowired
    VerifyService verifyService;


    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public JwbUser getuser(@PathVariable("id") String id) {
        return jwbUserMapper.selectById(id);
    }

    /*@RequestMapping("/r/r1")
    @PreAuthorize("hasAnyAuthority('p1')")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    @PreAuthorize("hasAuthority('p2')")
    public String r2() {
        return "访问r2资源";
    }*/

    @ApiOperation(value = "找回密码", tags = "找回密码")
    @RequestMapping("/findpassword")
    public void findPassword(@RequestBody FindPswDto findPswDto) {
        verifyService.findPassword(findPswDto);
    }

    @ApiOperation(value = "注册", tags = "注册")
    @PostMapping("/register")
    public void register(@RequestBody RegisterDto registerDto) {
        verifyService.register(registerDto);
    }
}
