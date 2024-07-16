package com.jwb.ucenter.service;

import com.jwb.ucenter.model.dto.AuthParamsDto;
import com.jwb.ucenter.model.dto.JwbUserExt;
import com.jwb.ucenter.model.po.JwbUser;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;

import java.util.List;

/**
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0.0
 * @date 2020/6/27 22:39
 * @since 1.0.0
 */
public interface UserService {

    AuthUser save(AuthUser user);

    AuthUser getByUuid(String uuid);

    List<AuthUser> listAll();

    void remove(String uuid);

    JwbUserExt execute(AuthParamsDto authParamsDto);

    JwbUser addUser(AuthUser user);

    AuthRequest getAuthRequest(String source);
}
