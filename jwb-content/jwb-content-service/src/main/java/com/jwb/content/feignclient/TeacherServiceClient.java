package com.jwb.content.feignclient;

import com.jwb.content.model.po.JwbTeacher;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(name = "company-api", fallbackFactory = TeacherServiceClientFallbackFactory.class)
public interface TeacherServiceClient {
    @ResponseBody
    @GetMapping("/company/teacher/{id}")
    JwbTeacher getTeacher(@PathVariable("id") String id);
}
