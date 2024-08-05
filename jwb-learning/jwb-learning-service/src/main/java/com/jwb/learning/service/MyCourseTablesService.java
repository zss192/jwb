package com.jwb.learning.service;

import com.jwb.base.model.PageResult;
import com.jwb.learning.model.dto.JwbChooseCourseDto;
import com.jwb.learning.model.dto.JwbCourseTablesDto;
import com.jwb.learning.model.dto.MyCourseTableItemDto;
import com.jwb.learning.model.dto.MyCourseTableParams;

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

    /**
     * 根据选课id更新选课表支付状态为已支付并插入选课表
     *
     * @param chooseCourseId
     */
    boolean saveChooseCourseStatus(String chooseCourseId);

    /**
     * 我的课程表
     * 使用 CompletableFuture版本
     *
     * @param params
     */
    PageResult<MyCourseTableItemDto> myCourseTables(MyCourseTableParams params);

    /**
     * 我的课程表
     * 不使用 CompletableFuture版本（用作查询对比）
     *
     * @param params
     */
    PageResult<MyCourseTableItemDto> myCourseTablesOld(MyCourseTableParams params);
}