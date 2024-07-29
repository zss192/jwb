package com.jwb.content.feignclient;

import com.jwb.content.model.po.JwbTeacher;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Component
public class TeacherServiceClientFallbackFactory implements FallbackFactory<TeacherServiceClient> {
    @Override
    public TeacherServiceClient create(Throwable throwable) {
        return new TeacherServiceClient() {
            @Override
            public JwbTeacher getTeacher(@RequestParam(value = "id") String id) {
                log.debug("熔断处理，熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
