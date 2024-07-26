package com.jwb.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.dto.ModifyDto;
import com.jwb.ucenter.model.dto.ModifyPasswordDto;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    JwbUserMapper jwbUserMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void modify(ModifyDto modifyDto) {
        JwbUser jwbUser = jwbUserMapper.selectById(modifyDto.getId());
        // 不允许更改邮箱
        if (!jwbUser.getEmail().equals(modifyDto.getEmail())) {
            throw new RuntimeException("不允许更改邮箱");
        }
        // 若更改了账号查询是否账号已存在
        if (!jwbUser.getUsername().equals(modifyDto.getUsername())) {
            JwbUser user = jwbUserMapper.selectOne(new LambdaQueryWrapper<JwbUser>()
                    .eq(JwbUser::getUsername, modifyDto.getUsername()));
            if (!user.getId().equals(jwbUser.getId())) {
                throw new RuntimeException("账号已存在，不允许重复");
            }
        }
        BeanUtils.copyProperties(modifyDto, jwbUser);
        jwbUserMapper.updateById(jwbUser);
    }

    @Override
    public ModifyDto getUserInfo(String id) {
        JwbUser jwbUser = jwbUserMapper.selectById(id);
        ModifyDto modifyDto = new ModifyDto();
        BeanUtils.copyProperties(jwbUser, modifyDto);
        return modifyDto;
    }

    @Override
    public void modifyPassword(ModifyPasswordDto modifyPasswordDto) {
        JwbUser jwbUser = jwbUserMapper.selectById(modifyPasswordDto.getId());
        // 如果是第三方登录第一次设置密码则不检查原始密码
        if (jwbUser.getSource() == null) {
            String originPassword = jwbUser.getPassword();
            String password = modifyPasswordDto.getPassword();
            boolean matches = passwordEncoder.matches(password, originPassword);
            if (!matches) {
                throw new RuntimeException("原始密码错误");
            }
        }
        String newPassword = modifyPasswordDto.getNewPassword();
        newPassword = new BCryptPasswordEncoder().encode(newPassword);
        jwbUser.setPassword(newPassword);
        jwbUserMapper.updateById(jwbUser);
    }
}
