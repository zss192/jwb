package com.jwb.content.service.jobhandler;

import com.jwb.base.utils.BloomFilterUtil;
import com.jwb.content.bloom.BloomFilterInit;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BloomFilterTask {
    @Autowired
    private BloomFilterUtil bloomFilterUtil;
    @Autowired
    private BloomFilterInit bloomFilterInit;

    @XxlJob("BloomFilterJobHandler")
    private void bloomFilterJobHandler() {
        log.debug("开始执行布隆过滤器重构任务");
        // 清空布隆过滤器，重新初始化
        bloomFilterUtil.clear();
        bloomFilterInit.initializeBloomFilter();
    }
}
