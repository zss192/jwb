package com.jwb.orders.feignclient;

import com.jwb.orders.model.dto.CourseBaseInfoDto;
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
            public CourseBaseInfoDto getCourseBaseById(@PathVariable("courseId") Long courseId) {
                log.debug("熔断处理，熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
