package com.jwb.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.feignclient.CheckCodeClient;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    JwbUserMapper jwbUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public JwbUserExt execute(AuthParamsDto authParamsDto) {
        // 校验验证码
        /*String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey)) {
            throw new RuntimeException("验证码为空");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }*/

        // 1. 获取账号或邮箱
        String username = authParamsDto.getUsername();
        // 2. 根据账号去数据库中查询是否存在
        JwbUser jwbUser = jwbUserMapper.selectOne(new LambdaQueryWrapper<JwbUser>().eq(JwbUser::getUsername, username).or()
                .eq(JwbUser::getEmail, username));
        // 3. 不存在抛异常 为了安全考虑不提示账号不存在
        if (jwbUser == null) {
            throw new RuntimeException("账号或密码错误");
        }
        // 4. 校验密码
        // 4.1 获取用户输入的密码
        String passwordForm = authParamsDto.getPassword();
        // 4.2 获取数据库中存储的密码
        String passwordDb = jwbUser.getPassword();
        // 4.3 比较密码
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        // 4.4 不匹配，抛异常
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        // 4.6 更新登录时间
        jwbUser.setUpdateTime(LocalDateTime.now());
        jwbUserMapper.updateById(jwbUser);
        // 4.5 匹配，封装返回
        JwbUserExt jwbUserExt = new JwbUserExt();
        BeanUtils.copyProperties(jwbUser, jwbUserExt);
        return jwbUserExt;
    }
}
