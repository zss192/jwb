package com.jwb.ucenter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.ucenter.cache.AuthStateRedisCache;
import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.mapper.JwbUserRoleMapper;
import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.model.po.JwbUserRole;
import com.jwb.ucenter.service.AuthService;
import com.jwb.ucenter.service.UserService;
import com.xkcoding.http.config.HttpConfig;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.scope.AuthGiteeScope;
import me.zhyd.oauth.enums.scope.AuthGithubScope;
import me.zhyd.oauth.enums.scope.AuthGoogleScope;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import me.zhyd.oauth.utils.AuthScopeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * JustAuth第三方登录
 */
@Service("user_authservice")
public class UserServiceImpl implements UserService, AuthService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    JwbUserMapper jwbUserMapper;
    @Autowired
    JwbUserRoleMapper jwbUserRoleMapper;
    @Autowired
    private AuthStateRedisCache stateRedisCache;

    @Value("${auth.redirectUrl}")
    String redirectUrl;
    @Value("${auth.wechat_open.clientId}")
    String wechat_open_id;
    @Value("${auth.wechat_open.clientSecret}")
    String wechat_open_secret;
    @Value("${auth.gitee.clientId}")
    String gitee_id;
    @Value("${auth.gitee.clientSecret}")
    String gitee_secret;
    @Value("${auth.github.clientId}")
    String github_id;
    @Value("${auth.github.clientSecret}")
    String github_secret;
    @Value("${auth.qq.clientId}")
    String qq_id;
    @Value("${auth.qq.clientSecret}")
    String qq_secret;
    @Value("${auth.google.clientId}")
    String google_id;
    @Value("${auth.google.clientSecret}")
    String google_secret;

    private BoundHashOperations<String, String, AuthUser> valueOperations;

    @PostConstruct
    public void init() {
        valueOperations = redisTemplate.boundHashOps("JUSTAUTH::USERS");
    }

    @Override
    public AuthUser save(AuthUser user) {
        valueOperations.put(user.getUuid(), user);
        return user;
    }

    @Override
    public AuthUser getByUuid(String uuid) {
        Object user = valueOperations.get(uuid);
        if (null == user) {
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(user), AuthUser.class);
    }

    @Override
    public List<AuthUser> listAll() {
        return new LinkedList<>(Objects.requireNonNull(valueOperations.values()));
    }

    @Override
    public void remove(String uuid) {
        valueOperations.delete(uuid);
    }

    /**
     * 保存用户到数据库
     *
     * @param user
     */
    @Override
    public JwbUser addUser(AuthUser user) {
        // 1. 获取用户唯一标识：unionid作为用户的唯一表示
        String unionid = user.getUuid();
        // 2. 根据唯一标识，判断数据库是否存在该用户
        JwbUser jwbUser = jwbUserMapper.selectOne(new LambdaQueryWrapper<JwbUser>().eq(JwbUser::getThirdUnionid, unionid));
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
        jwbUser.setThirdUnionid(unionid);
        jwbUser.setNickname(user.getNickname());
        jwbUser.setUserpic(user.getAvatar());
        jwbUser.setSource(user.getSource());
        jwbUser.setUtype("101001");  // 学生类型
        jwbUser.setStatus("1");
        jwbUser.setCompanyId("1232141425");
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
     * 根据具体的授权来源，获取授权请求工具类
     *
     * @param source
     * @return
     */
    @Override
    public AuthRequest getAuthRequest(String source) {
        AuthRequest authRequest = null;
        switch (source.toLowerCase()) {
            case "github":
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .clientId(github_id)
                        .clientSecret(github_secret)
                        .redirectUri(redirectUrl + "github")
                        .scopes(AuthScopeUtils.getScopes(AuthGithubScope.READ_USER))
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)))
                                .build())
                        .build(), stateRedisCache);
                break;
            case "gitee":
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId(gitee_id)
                        .clientSecret(gitee_secret)
                        .redirectUri(redirectUrl + "gitee")
                        .scopes(AuthScopeUtils.getScopes(AuthGiteeScope.USER_INFO))
                        .build(), stateRedisCache);
                break;
            case "qq":
                authRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId(qq_id)
                        .clientSecret(qq_secret)
                        .redirectUri(redirectUrl + "qq")
                        .build());
                break;
            case "wechat_open":
                authRequest = new AuthWeChatOpenRequest(AuthConfig.builder()
                        .clientId(wechat_open_id)
                        .clientSecret(wechat_open_secret)
                        .redirectUri(redirectUrl + "wechat_open")
                        .build());
                break;
            case "google":
                authRequest = new AuthGoogleRequest(AuthConfig.builder()
                        .clientId(google_id)
                        .clientSecret(google_secret)
                        .redirectUri(redirectUrl + "google")
                        .scopes(AuthScopeUtils.getScopes(AuthGoogleScope.USER_EMAIL, AuthGoogleScope.USER_PROFILE, AuthGoogleScope.USER_OPENID))
                        // 针对国外平台配置代理
                        .httpConfig(HttpConfig.builder()
                                .timeout(15000)
                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)))
                                .build())
                        .build());
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException("未获取到有效的Auth配置");
        }
        return authRequest;
    }

    /**
     * 第三方登录认证，不需要校验密码和验证码
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
        // 更新登录时间
        user.setUpdateTime(LocalDateTime.now());
        jwbUserMapper.updateById(user);
        
        JwbUserExt jwbUserExt = new JwbUserExt();
        BeanUtils.copyProperties(user, jwbUserExt);
        return jwbUserExt;
    }
}
