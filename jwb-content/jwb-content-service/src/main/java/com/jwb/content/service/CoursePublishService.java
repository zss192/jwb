package com.jwb.content.service;

import com.jwb.content.model.dto.CoursePreviewDto;

/**
 * 课程预览、发布接口
 */
public interface CoursePublishService {
    /**
     * 根据课程id获取课程预览信息
     *
     * @param courseId 课程id
     * @return package com.jwb.content.model.dto.CoursePreviewDto;
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
