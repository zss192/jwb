package com.jwb.search.mq;

import com.jwb.search.feignclient.ContentServiceClient;
import com.jwb.search.po.CourseIndex;
import com.jwb.search.po.CoursePublish;
import com.jwb.search.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CourseListener {
    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    private IndexService indexService;
    @Autowired
    private ContentServiceClient contentServiceClient;

    /**
     * 监听课程删除业务
     *
     * @param id 课程的id
     */
    @RabbitListener(queues = "course.delete.queue")
    public void listenHotelDeleteQueue(Long id) {
        indexService.deleteCourseIndex(courseIndexStore, id.toString());
    }

    /**
     * 监听课程新增/修改业务
     *
     * @param id 课程的id
     */
    @RabbitListener(queues = "course.insert.queue")
    public void listenHotelInsertQueue(Long id) {
        // 根据id查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(id);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        indexService.addCourseIndex(courseIndexStore, id.toString(), courseIndex);
    }
}
