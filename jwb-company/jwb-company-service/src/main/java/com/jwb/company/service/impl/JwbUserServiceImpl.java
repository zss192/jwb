package com.jwb.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.company.mapper.JwbUserMapper;
import com.jwb.company.model.po.JwbUser;
import com.jwb.company.service.JwbUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zss
 * @description 针对表【jwb_user】的数据库操作Service实现
 * @createDate 2024-07-23 19:37:58
 */
@Service
public class JwbUserServiceImpl extends ServiceImpl<JwbUserMapper, JwbUser>
        implements JwbUserService {
    @Autowired
    JwbUserMapper jwbUserMapper;

    @Override
    public PageResult<JwbUser> listMember(PageParams pageParams, Long companyId, String name, String cellphone, String status) {
        //构建查询条件对象
        LambdaQueryWrapper<JwbUser> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件
        queryWrapper.eq(JwbUser::getCompanyId, companyId);
        queryWrapper.like(StringUtils.isNotEmpty(name), JwbUser::getName, name);
        queryWrapper.like(StringUtils.isNotEmpty(cellphone), JwbUser::getCellphone, cellphone);
        queryWrapper.eq(StringUtils.isNotEmpty(status), JwbUser::getStatus, status);

        //分页对象
        Page<JwbUser> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<JwbUser> pageResult = jwbUserMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<JwbUser> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }
}




