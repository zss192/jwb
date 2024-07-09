package com.jwb.checkcode.service.impl;

import com.jwb.base.exception.JwbException;
import com.jwb.checkcode.service.SendCodeService;
import com.jwb.checkcode.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SendCodeServiceImpl implements SendCodeService {
    @Value("${mail.user}")
    String user;
    @Value("${mail.password}")
    String password;

    public final Long CODE_TTL = 180L;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public void sendEMail(String email, String code) {
        // 1. 向用户发送验证码
        try {
            MailUtil.sendTestMail(email, code, user, password);
        } catch (MessagingException e) {
            log.debug("邮件发送失败：{}", e.getMessage());
            JwbException.cast("发送验证码失败，请稍后再试");
        }
        // 2. 将验证码缓存到redis，TTL设置为2分钟
        redisTemplate.opsForValue().set(email, code, CODE_TTL, TimeUnit.SECONDS);
    }
}
