package com.jwb.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.model.dto.CourseCategoryTreeDto;
import com.jwb.model.po.CourseCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zss
 * @description 针对表【course_category(课程分类)】的数据库操作Mapper
 * @createDate 2024-06-12 15:34:05
 * @Entity generator.CourseCategory
 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    // 使用递归查询
    public List<CourseCategoryTreeDto> selectTreeNodes(String id);
}




