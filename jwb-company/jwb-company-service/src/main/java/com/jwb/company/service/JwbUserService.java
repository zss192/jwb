package com.jwb.company.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.company.model.po.JwbUser;

/**
 * @author zss
 * @description 针对表【jwb_user】的数据库操作Service
 * @createDate 2024-07-23 19:37:58
 */
public interface JwbUserService extends IService<JwbUser> {

    PageResult<JwbUser> listMember(PageParams pageParams, Long companyId, String name, String cellphone, String status);

    void changeStatus(String id, String status);
}
