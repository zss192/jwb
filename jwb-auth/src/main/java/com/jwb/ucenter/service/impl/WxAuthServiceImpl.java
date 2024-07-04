package com.jwb.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.mapper.JwbUserRoleMapper;
import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.model.po.JwbUserRole;
import com.jwb.ucenter.service.AuthService;
import com.jwb.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    JwbUserMapper jwbUserMapper;
    @Autowired
    JwbUserRoleMapper jwbUserRoleMapper;

    @Autowired
    WxAuthServiceImpl wxAuthService;

    @Autowired
    RestTemplate restTemplate;

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    @Override
    public JwbUser wxAuth(String code) {
        //第一步：根据code获取access_token
        Map<String, String> access_token_map = getAccess_token(code);
        String accessToken = access_token_map.get("access_token");
        //第二步：根据得到的access_token获取用户信息
        String openid = access_token_map.get("openid");
        Map<String, String> user_info_map = getUserInfo(accessToken, openid);
        //第三步：添加用户信息到数据库
        return wxAuthService.addWxUser(user_info_map);
    }

    /**
     * 根据code申请令牌access_token
     *
     * @param code
     */
    private Map<String, String> getAccess_token(String code) {
        // 1. 请求路径模板，参数用%s占位符
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 2. 填充占位符：appid，secret，code
        String url = String.format(url_template, appid, secret, code);
        // 3. 远程调用URL，POST方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 4. 获取相应结果，响应结果为json格式
        String result = exchange.getBody();
        // 5. 转为map
        return JSON.parseObject(result, Map.class);
    }

    /**
     * 根据令牌access_token取查询用户信息
     *
     * @param access_token
     * @param openid
     */
    private Map<String, String> getUserInfo(String access_token, String openid) {
        // 1. 请求路径模板，参数用%s占位符
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        // 2. 填充占位符，access_token和openid
        String url = String.format(url_template, access_token, openid);
        // 3. 远程调用URL，GET方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        // 4. 获取响应结果，JSON格式
        String result = exchange.getBody();
        // 4.1 有中文，需要转码
        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // 5. 转为map
        return JSON.parseObject(result, Map.class);
    }

    public JwbUser addWxUser(Map<String, String> user_info_map) {
        // 1. 获取用户唯一标识：unionid作为用户的唯一表示
        String unionid = user_info_map.get("unionid");
        // 2. 根据唯一标识，判断数据库是否存在该用户
        JwbUser jwbUser = jwbUserMapper.selectOne(new LambdaQueryWrapper<JwbUser>().eq(JwbUser::getWxUnionid, unionid));
        // 2.1 存在，则直接返回
        if (jwbUser != null) {
            return jwbUser;
        }
        // 2.2 不存在，新增
        jwbUser = new JwbUser();
        // 2.3 设置主键
        String uuid = UUID.randomUUID().toString();
        jwbUser.setId(uuid);
        // 2.4 设置其他数据库非空约束的属性
        jwbUser.setUsername(unionid);
        // TODO：弹框让绑定手机号并设置密码，参考：https://www.fotor.com.cn/
        jwbUser.setPassword(unionid);
        jwbUser.setWxUnionid(unionid);
        jwbUser.setNickname(user_info_map.get("nickname"));
        jwbUser.setUserpic(user_info_map.get("headimgurl"));
        jwbUser.setName(user_info_map.get("nickname"));
        jwbUser.setUtype("101001");  // 学生类型
        jwbUser.setStatus("1");
        jwbUser.setCreateTime(LocalDateTime.now());
        // 2.5 添加到数据库
        jwbUserMapper.insert(jwbUser);
        // 3. 添加用户信息到用户角色表
        JwbUserRole jwbUserRole = new JwbUserRole();
        jwbUserRole.setId(uuid);
        jwbUserRole.setUserId(uuid);
        jwbUserRole.setRoleId("17");
        jwbUserRole.setCreateTime(LocalDateTime.now());
        jwbUserRoleMapper.insert(jwbUserRole);
        return jwbUser;
    }

    /**
     * 微信扫码认证，不需要校验密码和验证码
     *
     * @param authParamsDto 认证参数
     */
    @Override
    public JwbUserExt execute(AuthParamsDto authParamsDto) {
        // 账号
        String username = authParamsDto.getUsername();
        JwbUser user = jwbUserMapper.selectOne(new LambdaQueryWrapper<JwbUser>().eq(JwbUser::getUsername, username));
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        JwbUserExt jwbUserExt = new JwbUserExt();
        BeanUtils.copyProperties(user, jwbUserExt);
        return jwbUserExt;
    }
}