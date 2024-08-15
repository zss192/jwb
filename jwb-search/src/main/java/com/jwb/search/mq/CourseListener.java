package com.jwb.search.mq;

import com.alibaba.fastjson.JSON;
import com.jwb.search.dto.CanalMessage;
import com.jwb.search.po.CourseIndex;
import com.jwb.search.po.CoursePublish;
import com.jwb.search.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CourseListener {
    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    private IndexService indexService;

    /**
     * 监听课程变化业务
     */
    @RabbitListener(queues = "course.canal.queue")
    public void listenCourseQueue(Message message) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        CanalMessage canalMessage = JSON.parseObject(msg, CanalMessage.class);

        CoursePublish coursePublish = JSON.parseObject(String.valueOf(canalMessage.getData().get(0)), CoursePublish.class);
        Long courseId = coursePublish.getId();
        String type = canalMessage.getType();

        if ("DELETE".equals(type)) {
            indexService.deleteCourseIndex(courseIndexStore, courseId.toString());
        } else if ("INSERT".equals(type) || "UPDATE".equals(type)) {
            CourseIndex courseIndex = new CourseIndex();
            BeanUtils.copyProperties(coursePublish, courseIndex);
            indexService.addCourseIndex(courseIndexStore, courseId.toString(), courseIndex);
        }
    }
}
