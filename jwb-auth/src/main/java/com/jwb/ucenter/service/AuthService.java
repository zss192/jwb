package com.jwb.ucenter.service;

import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;

/**
 * 认证Service
 */
public interface AuthService {
    // 设计模式：策略模式，两个实现类扫码登录和用户密码

    /**
     * 认证方法
     *
     * @param authParamsDto 认证参数
     * @return 用户信息
     */
    JwbUserExt execute(AuthParamsDto authParamsDto);
}
