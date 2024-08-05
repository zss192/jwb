package com.jwb.learning.feignclient;

import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.model.po.CourseScore;
import com.jwb.content.model.po.CourseTeacher;
import com.jwb.content.model.po.Teachplan;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

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

            @Override
            public Map<Long, CoursePublish> getCoursePublishBatch(ArrayList<Long> courseIds) {
                log.error("调用内容管理服务查询课程发布信息发生熔断:{}", throwable.toString(), throwable);
                return null;
            }

            @Override
            public Map<Long, CourseScore> getCourseScoreBatch(ArrayList<Long> courseIds) {
                log.error("调用内容管理服务查询课程评分发生熔断:{}", throwable.toString(), throwable);
                return null;
            }

            @Override
            public Map<Long, CourseTeacher> getCourseTeacherBatch(ArrayList<Long> courseIds) {
                log.error("调用内容管理服务查询课程教师发生熔断:{}", throwable.toString(), throwable);
                return null;
            }
        };
    }
}
