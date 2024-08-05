package com.jwb.content.api;

import com.jwb.content.model.po.CourseTeacher;
import com.jwb.content.model.po.JwbTeacher;
import com.jwb.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
@Api(value = "教师信息相关接口", tags = "教师信息相关接口")
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;


    @ApiOperation("查询教师信息接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public JwbTeacher getCourseTeacherList(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeacherList(courseId);
    }

    @ApiOperation("批量查询教师信息接口")
    @GetMapping("/courseTeacher/batch")
    public Map<Long, CourseTeacher> getCourseTeacherBatch(@RequestParam("courseIds") ArrayList<Long> courseIds) {
        return courseTeacherService.getCourseTeacherBatch(courseIds);
    }

    @ApiOperation("更新教师信息接口")
    @PutMapping("/updateTeacher/{courseBaseId}/{courseTeacherId}")
    public void updateCourseTeacher(@PathVariable Long courseBaseId, @PathVariable Long courseTeacherId) {
        courseTeacherService.updateTeacher(courseBaseId, courseTeacherId);
    }


    @ApiOperation("删除教师信息接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, teacherId);
    }
}
