package com.jwb.company.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.company.mapper.JwbCompanyMapper;
import com.jwb.company.model.dto.EditCompanyDto;
import com.jwb.company.model.po.JwbCompany;
import com.jwb.company.service.JwbCompanyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zss
 * @description 针对表【jwb_company】的数据库操作Service实现
 * @createDate 2024-07-23 14:58:32
 */
@Service
public class JwbCompanyServiceImpl extends ServiceImpl<JwbCompanyMapper, JwbCompany>
        implements JwbCompanyService {

    @Autowired
    JwbCompanyMapper companyMapper;

    @Override
    public JwbCompany queryCompany(Long companyId) {
        return companyMapper.selectById(companyId);
    }

    @Override
    public void modifyCompany(EditCompanyDto editCompanyDto) {
        JwbCompany jwbCompany = new JwbCompany();
        BeanUtils.copyProperties(editCompanyDto, jwbCompany);
        companyMapper.updateById(jwbCompany);
    }
}




