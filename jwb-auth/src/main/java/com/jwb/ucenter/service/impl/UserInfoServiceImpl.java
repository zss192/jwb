package com.jwb.ucenter.service.impl;

import com.jwb.ucenter.mapper.JwbUserMapper;
import com.jwb.ucenter.model.dto.ModifyDto;
import com.jwb.ucenter.model.po.JwbUser;
import com.jwb.ucenter.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    JwbUserMapper jwbUserMapper;

    @Override
    public void modify(ModifyDto modifyDto) {
        JwbUser jwbUser = jwbUserMapper.selectById(modifyDto.getId());
        BeanUtils.copyProperties(modifyDto, jwbUser);
        jwbUserMapper.updateById(jwbUser);
    }

    @Override
    public ModifyDto getUserInfo(String id) {
        JwbUser jwbUser = jwbUserMapper.selectById(id);
        ModifyDto modifyDto = new ModifyDto();
        BeanUtils.copyProperties(jwbUser, modifyDto);
        return modifyDto;
    }
}
