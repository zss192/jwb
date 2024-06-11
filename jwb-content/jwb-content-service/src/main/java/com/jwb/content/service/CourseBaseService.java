package com.jwb.content.service;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.model.dto.QueryCourseParamsDto;
import com.jwb.model.po.CourseBase;

/**
 * @author zss
 * @description 针对表【course_base(课程基本信息)】的数据库操作Service
 * @createDate 2024-06-11 16:24:40
 */
public interface CourseBaseService {
    // 课程分页查询
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
