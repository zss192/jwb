package com.jwb.content.service;

import com.jwb.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author zss
 * @description 针对表【course_category(课程分类)】的数据库操作Service
 * @createDate 2024-06-12 15:39:43
 */
public interface CourseCategoryService {
    /**
     * 课程分类树查询
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
