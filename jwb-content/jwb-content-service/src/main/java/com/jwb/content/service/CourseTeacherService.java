package com.jwb.content.service;

import com.jwb.content.model.po.JwbTeacher;

public interface CourseTeacherService {
    JwbTeacher getCourseTeacherList(Long courseId);

    void deleteCourseTeacher(Long courseId, Long teacherId);

    void updateTeacher(Long courseBaseId, Long courseTeacherId);
}