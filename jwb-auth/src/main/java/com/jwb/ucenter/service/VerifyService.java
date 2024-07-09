package com.jwb.ucenter.service;

import com.jwb.ucenter.model.dto.FindPswDto;
import com.jwb.ucenter.model.dto.RegisterDto;

public interface VerifyService {
    void findPassword(FindPswDto findPswDto);

    void register(RegisterDto registerDto);
}
