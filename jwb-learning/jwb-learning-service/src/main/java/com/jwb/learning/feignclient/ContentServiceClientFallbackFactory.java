package com.jwb.learning.feignclient;

import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.model.po.Teachplan;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {

            @Override
            public CoursePublish getCoursePublish(Long courseId) {
                log.error("调用内容管理服务发生熔断:{}", throwable.toString(), throwable);
                return null;
            }

            @Override
            public Teachplan getTeachplan(Long teachplanId) {
                log.error("调用内容管理服务查询教学计划发生熔断:{}", throwable.toString(), throwable);
                return null;
            }

            @Override
            public void addStudyCount(Long courseId) {
                log.error("调用内容管理服务查询教学计划发生熔断:{}", throwable.toString(), throwable);
            }
        };
    }
}
