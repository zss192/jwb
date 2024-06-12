package com.jwb.model.dto;

import com.jwb.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 课程分类(CourseCategory)Dto
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}

