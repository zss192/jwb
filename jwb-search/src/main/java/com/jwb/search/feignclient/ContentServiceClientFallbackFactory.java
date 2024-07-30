package com.jwb.search.feignclient;

import com.jwb.search.po.CoursePublish;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId) {
                log.debug("熔断处理，熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
