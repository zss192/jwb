package com.jwb.content.api;

import com.jwb.content.model.dto.CoursePreviewDto;
import com.jwb.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/open")
@Api(value = "课程计划信息相关接口", tags = "课程计划信息相关接口")
public class CourseOpenController {
    @Autowired
    private CoursePublishService coursePublishService;

    @ApiOperation("查询课程计划信息")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable Long courseId) {
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
