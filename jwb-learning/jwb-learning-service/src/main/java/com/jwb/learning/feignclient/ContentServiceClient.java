package com.jwb.learning.feignclient;

import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.model.po.CourseScore;
import com.jwb.content.model.po.CourseTeacher;
import com.jwb.content.model.po.Teachplan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * @description 内容管理远程接口
 */
@FeignClient(value = "content-api", fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/content/r/coursepublish/{courseId}")
    CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId);

    @PostMapping("/content/teachplan/{teachplanId}")
    Teachplan getTeachplan(@PathVariable("teachplanId") Long teachplanId);

    @PutMapping("/content/course/addStudyCount/{courseId}")
    void addStudyCount(@PathVariable("courseId") Long courseId);

    @ResponseBody
    @GetMapping("/content/r/coursepublish/batch")
    Map<Long, CoursePublish> getCoursePublishBatch(@RequestParam("courseIds") ArrayList<Long> courseIds);

    @ResponseBody
    @GetMapping("/content/course-comment/getCourseScore/batch")
    Map<Long, CourseScore> getCourseScoreBatch(@RequestParam("courseIds") ArrayList<Long> courseIds);

    @ResponseBody
    @GetMapping("/content/courseTeacher/batch")
    Map<Long, CourseTeacher> getCourseTeacherBatch(@RequestParam("courseIds") ArrayList<Long> courseIds);
}
