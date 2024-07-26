package com.jwb.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.mapper.JwbUserRoleMapper;
import com.jwb.ucenter.model.dto.FindPswDto;
import com.jwb.ucenter.model.dto.RegisterDto;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.model.po.JwbUserRole;
import com.jwb.ucenter.service.VerifyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Transactional
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

        LambdaQueryWrapper<JwbUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(JwbUser::getEmail, findPswDto.getEmail());
        JwbUser user = userMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userMapper.updateById(user);
        redisTemplate.delete(email);
    }

    @Override
    @Transactional
    public void register(RegisterDto registerDto) {
        String uuid = UUID.randomUUID().toString();
        String email = registerDto.getEmail();
        String checkcode = registerDto.getCheckcode();

        // 1. 从redis中获取缓存的验证码
        String codeInRedis = redisTemplate.opsForValue().get(email);
        if (codeInRedis == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        // 2. 判断是否与用户输入的一致
        if (!codeInRedis.equalsIgnoreCase(checkcode)) {
            throw new RuntimeException("验证码输入错误");
        }

        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        LambdaQueryWrapper<JwbUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(JwbUser::getEmail, registerDto.getEmail());
        JwbUser user = userMapper.selectOne(lambdaQueryWrapper);
        if (user != null) {
            throw new RuntimeException("用户已存在，一个邮箱只能注册一个账号");
        }
        lambdaQueryWrapper.eq(JwbUser::getUsername, registerDto.getUsername());
        user = userMapper.selectOne(lambdaQueryWrapper);
        if (user != null) {
            throw new RuntimeException("账号已存在，不允许重复");
        }
        JwbUser jwbUser = new JwbUser();
        BeanUtils.copyProperties(registerDto, jwbUser);
        jwbUser.setPassword(new BCryptPasswordEncoder().encode(password));
        jwbUser.setId(uuid);
        jwbUser.setUtype("101001");  // 学生类型
        jwbUser.setStatus("1");
        jwbUser.setNickname(registerDto.getUsername());
        jwbUser.setCreateTime(LocalDateTime.now());
        int insert = userMapper.insert(jwbUser);
        if (insert <= 0) {
            throw new RuntimeException("新增用户信息失败");
        }

        JwbUserRole jwbUserRole = new JwbUserRole();
        jwbUserRole.setId(uuid);
        jwbUserRole.setUserId(uuid);
        jwbUserRole.setRoleId("17");
        jwbUserRole.setCreateTime(LocalDateTime.now());
        int insert1 = jwbUserRoleMapper.insert(jwbUserRole);
        if (insert1 <= 0) {
            throw new RuntimeException("新增用户角色信息失败");
        }
    }
}
