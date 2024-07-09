package com.jwb.search;

import com.jwb.search.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description 课程索引与搜索测试
 */
@SpringBootTest
public class CourseIndexSearchTest {


    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    IndexService courseIndexService;


}
