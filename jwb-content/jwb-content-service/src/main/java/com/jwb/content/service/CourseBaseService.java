package com.jwb.content.service;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.model.dto.*;
import com.jwb.content.model.po.CourseBase;

/**
 * @author zss
 * @description 针对表【course_base(课程基本信息)】的数据库操作Service
 * @createDate 2024-06-11 16:24:40
 */
public interface CourseBaseService {
    // 课程分页查询
    PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    // 添加课程
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    // 根据课程id查询课程信息
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    // 修改课程信息
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    void delectCourse(Long companyId, Long courseId);

    void addStudyCount(Long companyId, Long courseId);

    /**
     * 获取课程动态信息
     *
     * @param courseId 课程id
     * @return 课程动态信息
     */
    CourseDynamicDto getCourseDynamicInfo(Long courseId);
}
