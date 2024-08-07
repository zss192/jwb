package com.jwb.company.controller;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.company.model.dto.EditCompanyDto;
import com.jwb.company.model.po.JwbCompany;
import com.jwb.company.model.po.JwbUser;
import com.jwb.company.service.JwbCompanyService;
import com.jwb.company.service.JwbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(value = "机构管理接口", tags = "机构管理接口")
public class CompanyController {
    @Autowired
    JwbCompanyService companyService;
    @Autowired
    JwbUserService userService;

    @GetMapping("/my-company")
    @ApiOperation("查询机构资料")
    public JwbCompany queryCompany() {
        Long companyId = 1232141425L;
        return companyService.queryCompany(companyId);
    }

    @PreAuthorize("hasAuthority('jwb_sysmanager_company')")
    @PutMapping("/company")
    @ApiOperation("修改机构资料")
    public void queryCompany(@RequestBody EditCompanyDto editCompanyDto) {
        companyService.modifyCompany(editCompanyDto);
    }

    @GetMapping("/member/list")
    @ApiOperation("查询机构成员")
    public PageResult<JwbUser> listMember(PageParams pageParams, String username, String cellphone, String status) {
        Long companyId = 1232141425L;
        return userService.listMember(pageParams, companyId, username, cellphone, status);
    }

    // TODO：封禁的用户禁止登录并且立马清除其jwt
    @PutMapping("/member/status/{id}/{status}")
    @ApiOperation("更改成员状态")
    public void changeStatus(@PathVariable String id, @PathVariable String status) {
        userService.changeStatus(id, status);
    }
}
