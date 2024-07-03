package com.jwb.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.po.JwbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsImpl implements UserDetailsService {
    @Autowired
    JwbUserMapper jwbUserMapper;

    /**
     * @param name 用户输入的登录账号
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        // 根据username去XcUser表中查询对应的用户信息
        JwbUser user = jwbUserMapper.selectOne(new LambdaQueryWrapper<JwbUser>().eq(JwbUser::getUsername, name));
        // 返回NULL表示用户不存在，SpringSecurity会帮我们处理，框架抛出异常用户不存在
        if (user == null) {
            return null;
        }
        // 取出数据库存储的密码
        String password = user.getPassword();
        // 用户敏感信息不要设置
        user.setPassword(null);
        String userString = JSON.toJSONString(user);
        //如果查到了用户拿到正确的密码，最终封装成一个UserDetails对象给spring security框架返回，由框架进行密码比对
        // username存整个用户信息以此扩展jwt中的用户信息
        return User.withUsername(userString).password(password).authorities("test").build();
    }
}