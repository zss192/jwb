package com.jwb.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseFavorite;

import java.util.List;

/**
 * @author zss
 * @description 针对表【course_favorite】的数据库操作Mapper
 * @createDate 2024-07-31 10:48:35
 * @Entity com.jwb.content.CourseFavorite
 */
public interface CourseFavoriteMapper extends BaseMapper<CourseFavorite> {

    /**
     * 查询用户收藏课程列表
     *
     * @param userId 用户id
     * @return 用户收藏课程列表
     */

    List<CourseBase> getCourseFavoriteList(Long userId);
}




