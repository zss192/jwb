package com.jwb.media.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Component
public class TeachplanClientFallbackFactory implements FallbackFactory<TeachplanClient> {
    @Override
    public TeachplanClient create(Throwable throwable) {
        return new TeachplanClient() {
            @Override
            public Boolean ifExistMedia(@PathVariable String mediaId) {
                log.debug("熔断处理，熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
