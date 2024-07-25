package com.jwb.ucenter.service;

import com.jwb.ucenter.model.dto.ModifyDto;

public interface UserInfoService {
    void modify(ModifyDto modifyDto);

    ModifyDto getUserInfo(String id);
}
