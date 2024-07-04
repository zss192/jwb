package com.jwb.ucenter.service;

import com.jwb.ucenter.model.po.JwbUser;

public interface WxAuthService {
    public JwbUser wxAuth(String code);
}
