package com.jwb.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.mapper.JwbUserRoleMapper;
import com.jwb.ucenter.model.dto.FindPswDto;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.VerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class VerifyServiceImpl implements VerifyService {

    @Autowired
    JwbUserMapper userMapper;
    @Autowired
    JwbUserRoleMapper jwbUserRoleMapper;
    @Autowired
    StringRedisTemplate redisTemplate;

    public Boolean verify(String email, String checkcode) {


        return false;
    }


    @Override
    public void findPassword(FindPswDto findPswDto) {
        String email = findPswDto.getEmail();
        String checkcode = findPswDto.getCheckcode();

        // 1. 从redis中获取缓存的验证码
        String codeInRedis = redisTemplate.opsForValue().get(email);
        if (codeInRedis == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        // 2. 判断是否与用户输入的一致
        if (!codeInRedis.equalsIgnoreCase(checkcode)) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        redisTemplate.delete(email);

        LambdaQueryWrapper<JwbUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(JwbUser::getEmail, findPswDto.getEmail());
        JwbUser user = userMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userMapper.updateById(user);
    }
}
