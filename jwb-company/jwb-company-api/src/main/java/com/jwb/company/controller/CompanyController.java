package com.jwb.company.controller;

import com.jwb.company.model.dto.EditCompanyDto;
import com.jwb.company.model.po.JwbCompany;
import com.jwb.company.service.JwbCompanyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(value = "机构管理接口", tags = "机构管理接口")
public class CompanyController {
    @Autowired
    JwbCompanyService companyService;


    @GetMapping("/my-company")
    @ApiOperation("查询机构资料")
    public JwbCompany queryCompany() {
        Long companyId = 1232141425L;
        return companyService.queryCompany(companyId);
    }

    @PutMapping("/company")
    @ApiOperation("修改机构资料")
    public void queryCompany(@RequestBody EditCompanyDto editCompanyDto) {
        companyService.modifyCompany(editCompanyDto);
    }
}
