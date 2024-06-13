package com.jwb.content.api;

import com.jwb.base.exception.ValidationGroups;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.model.dto.AddCourseDto;
import com.jwb.content.model.dto.CourseBaseInfoDto;
import com.jwb.content.model.dto.QueryCourseParamsDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {
    @Autowired
    CourseBaseService courseBaseService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        return courseBaseService.queryCourseBaseList(pageParams, queryCourseParams);
    }

    @ApiOperation("创建课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        // TODO:获取机构id，暂时固定数据
        Long companyId = 1232141425L;
        return courseBaseService.createCourseBase(companyId, addCourseDto);
    }

}
