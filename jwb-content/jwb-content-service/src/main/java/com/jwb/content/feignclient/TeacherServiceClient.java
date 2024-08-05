package com.jwb.content.feignclient;

import com.jwb.content.model.po.JwbTeacher;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Map;

@FeignClient(name = "company-api", fallbackFactory = TeacherServiceClientFallbackFactory.class)
public interface TeacherServiceClient {
    @ResponseBody
    @GetMapping("/company/teacher/{id}")
    JwbTeacher getTeacher(@PathVariable("id") String id);

    @ResponseBody
    @GetMapping("/company/teacher/list/batch")
    Map<Long, JwbTeacher> getTeacherBatch(@RequestParam("ids") ArrayList<Long> ids);
}
