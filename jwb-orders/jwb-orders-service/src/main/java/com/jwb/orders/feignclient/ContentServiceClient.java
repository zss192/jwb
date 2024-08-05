package com.jwb.orders.feignclient;

import com.jwb.orders.model.dto.CourseBaseInfoDto;
import com.jwb.orders.model.po.CourseScore;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "content-api", fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {
    @ResponseBody
    @GetMapping("/content/course/{courseId}")
    CourseBaseInfoDto getCourseBaseById(@PathVariable("courseId") Long courseId);

    @ResponseBody
    @GetMapping("/content/course-comment/getCourseScore/{courseId}")
    CourseScore getCourseScore(@PathVariable("courseId") Long courseId);
}
