package com.jwb.content.service;

import com.jwb.content.model.po.JwbTeacher;

import java.util.ArrayList;
import java.util.Map;

public interface CourseTeacherService {
    JwbTeacher getCourseTeacherList(Long courseId);

    void deleteCourseTeacher(Long courseId, Long teacherId);

    void updateTeacher(Long courseBaseId, Long courseTeacherId);

    /**
     * 批量查询教师信息
     *
     * @param courseIds 课程id列表
     * @return 课程id与教师信息的映射
     */
    Map<Long, JwbTeacher> getCourseTeacherBatch(ArrayList<Long> courseIds);
}