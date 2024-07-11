package com.jwb.learning.api;

import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageResult;
import com.jwb.learning.model.dto.JwbChooseCourseDto;
import com.jwb.learning.model.dto.JwbCourseTablesDto;
import com.jwb.learning.model.dto.MyCourseTableParams;
import com.jwb.learning.model.po.JwbCourseTables;
import com.jwb.learning.service.MyCourseTablesService;
import com.jwb.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public JwbChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        String userId = SecurityUtil.getUser().getId();
        return myCourseTablesService.addChooseCourse(userId, courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public JwbCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        String userId = SecurityUtil.getUser().getId();
        return myCourseTablesService.getLearningStatus(userId, courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<JwbCourseTables> mycoursetable(MyCourseTableParams params) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            JwbException.cast("请登录后查看课程表");
        }
        String userId = user.getId();
        params.setUserId(userId);
        return myCourseTablesService.myCourseTables(params);
    }

}
