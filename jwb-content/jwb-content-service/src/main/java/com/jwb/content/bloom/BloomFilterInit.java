package com.jwb.content.bloom;

import com.jwb.base.utils.BloomFilterUtil;
import com.jwb.content.mapper.CourseBaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 布隆过滤器初始化
 */
@Component
@Slf4j
public class BloomFilterInit {
    @Autowired
    private BloomFilterUtil bloomFilterUtil;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @PostConstruct
    public void initializeBloomFilter() {
        log.debug("初始化布隆过滤器");
        // 查询数据库获取全部数据的id
        List<String> ids = courseBaseMapper.getIds();

        // 将数据添加到布隆过滤器
        for (String id : ids) {
            bloomFilterUtil.put("course_dynamic:" + id);
        }
    }
}
