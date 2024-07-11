package com.jwb.learning.feignclient;

import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.model.po.Teachplan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
