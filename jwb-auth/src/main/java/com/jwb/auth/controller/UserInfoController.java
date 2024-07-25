package com.jwb.auth.controller;

import com.jwb.ucenter.model.dto.ModifyDto;
import com.jwb.ucenter.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(value = "用户信息接口", tags = "用户信息接口")
public class UserInfoController {
    @Autowired
    UserInfoService userInfoService;

    @ApiOperation(value = "查询用户信息", tags = "查询用户信息")
    @GetMapping("/getUserInfo/{id}")
    public ModifyDto getUserInfo(@PathVariable String id) {
        return userInfoService.getUserInfo(id);
    }

    @ApiOperation(value = "修改用户信息", tags = "修改用户信息")
    @PostMapping("/modify")
    public void modify(@RequestBody ModifyDto modifyDto) {
        userInfoService.modify(modifyDto);
    }
}
