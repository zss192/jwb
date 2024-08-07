package com.jwb.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.jwb.ucenter.mapper.JwbMenuMapper;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;
import com.jwb.ucenter.model.po.JwbMenu;
import com.jwb.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserDetailsImpl implements UserDetailsService {
    @Autowired
    JwbUserMapper jwbUserMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    JwbMenuMapper jwbMenuMapper;

    /**
     * @param s 用户输入的登录账号
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.error("认证请求数据格式不对：{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        // 获取认证类型，beanName就是 认证类型 + 后缀，例如 password + _authservice = password_authservice
        String authType = authParamsDto.getAuthType();
        // 根据认证类型，从Spring容器中取出对应的bean
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        JwbUserExt user = authService.execute(authParamsDto);

        return getUserPrincipal(user);
    }

    public UserDetails getUserPrincipal(JwbUserExt user) {
        // 设置用户权限
        String userId = user.getId();
        // 根据用户id查询用户权限
        List<JwbMenu> jwbMenus = jwbMenuMapper.selectPermissionByUserId(userId);
        List<String> permissions = new ArrayList<>();
        // 没权限，给一个默认的
        if (jwbMenus.isEmpty()) {
            permissions.add("test");
        } else {
            // 获取权限，加入到集合里
            jwbMenus.forEach(jwbMenu -> {
                permissions.add(jwbMenu.getCode());
            });
        }
        // 设置权限
        user.setPermissions(permissions);
        String[] authorities = permissions.toArray(new String[0]);

        // 取出数据库存储的密码
        String password = user.getPassword();
        // 用户敏感信息不要设置
        user.setPassword(null);

        String userJsonStr = JSON.toJSONString(user);
        //如果查到了用户拿到正确的密码，最终封装成一个UserDetails对象给spring security框架返回，由框架进行密码比对
        // username存整个用户信息以此扩展jwt中的用户信息
        return User.withUsername(userJsonStr).password(password).authorities(authorities).build();
    }
}