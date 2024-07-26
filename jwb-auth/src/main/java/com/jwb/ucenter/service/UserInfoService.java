package com.jwb.ucenter.service;

import com.jwb.ucenter.model.dto.ModifyDto;
import com.jwb.ucenter.model.dto.ModifyPasswordDto;

public interface UserInfoService {
    void modify(ModifyDto modifyDto);

    ModifyDto getUserInfo(String id);

    void modifyPassword(ModifyPasswordDto modifyPasswordDto);
}
