package com.jwb.content.api;

import com.jwb.content.model.dto.CourseCategoryTreeDto;
import com.jwb.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程分类
 */
@RestController
@Api(value = "课程分类接口", tags = "课程分类接口")
public class CourseCategoryController {
    @Autowired
    CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类信息查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }
}
