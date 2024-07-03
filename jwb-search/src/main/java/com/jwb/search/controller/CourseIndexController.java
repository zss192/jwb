package com.jwb.search.controller;

import com.jwb.base.exception.JwbException;
import com.jwb.search.po.CourseIndex;
import com.jwb.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "课程信息索引接口", tags = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    IndexService indexService;

    @ApiOperation("添加课程索引")
    @PostMapping("course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {

        Long id = courseIndex.getId();
        if (id == null) {
            JwbException.cast("课程id为空");
        }
        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);
        if (!result) {
            JwbException.cast("添加课程索引失败");
        }
        return result;

    }
}
