package com.jwb.media.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 课程计划服务远程调用接口
 */
@FeignClient(value = "content-api", fallbackFactory = TeachplanClientFallbackFactory.class)
public interface TeachplanClient {
    @GetMapping("/content/teachplan/ifExistMedia/{mediaId}")
    Boolean ifExistMedia(@PathVariable("mediaId") String mediaId);
}
