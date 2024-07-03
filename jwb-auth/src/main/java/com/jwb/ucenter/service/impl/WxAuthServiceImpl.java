package com.jwb.ucenter.service.impl;

import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;
import com.jwb.ucenter.service.AuthService;
import org.springframework.stereotype.Service;

@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService {

    @Override
    public JwbUserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }
}