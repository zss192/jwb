package com.jwb.learning.service;

import com.jwb.learning.model.dto.JwbChooseCourseDto;
import com.jwb.learning.model.dto.JwbCourseTablesDto;

public interface MyCourseTablesService {
    /**
     * 添加选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     */
    JwbChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 获取学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 学习资格状态
     */
    JwbCourseTablesDto getLearningStatus(String userId, Long courseId);
}