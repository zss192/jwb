package com.jwb.content.api;

import com.jwb.base.utils.SecurityUtil;
import com.jwb.content.model.dto.CoursePreviewDto;
import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@Api(value = "课程发布相关接口", tags = "课程发布相关接口")
public class CoursePublishController {
    @Autowired
    private CoursePublishService coursePublishService;

    @ApiOperation("查询课程预览信息")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("course_template");
        modelAndView.addObject("model", coursePreviewInfo);
        return modelAndView;
    }

    @ApiOperation("提交课程审核")
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable Long courseId) {
        SecurityUtil.JwbUser user = SecurityUtil.getUser();
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable Long courseId) {
        SecurityUtil.JwbUser user = SecurityUtil.getUser();
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        coursePublishService.publishCourse(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId) {
        return coursePublishService.getCoursePublish(courseId);
    }

    @ApiOperation("获取课程发布信息")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePreviewDto(@PathVariable("courseId") Long courseId) {
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
