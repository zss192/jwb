package com.jwb.company.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jwb.company.model.dto.EditCompanyDto;
import com.jwb.company.model.po.JwbCompany;

/**
 * @author zss
 * @description 针对表【jwb_company】的数据库操作Service
 * @createDate 2024-07-23 14:58:32
 */
public interface JwbCompanyService extends IService<JwbCompany> {

    JwbCompany queryCompany(Long companyId);

    void modifyCompany(EditCompanyDto editCompanyDto);
}
