package com.jwb.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseFavorite;

import java.util.List;

/**
 * @author zss
 * @description 针对表【course_favorite】的数据库操作Service
 * @createDate 2024-07-31 16:59:44
 */
public interface CourseFavoriteService extends IService<CourseFavorite> {

    /**
     * 课程收藏
     *
     * @param courseId   课程id
     * @param userId     用户id
     * @param isFavorite true:收藏 false:取消收藏
     */
    void courseFavorite(Long courseId, Long userId, Boolean isFavorite);

    /**
     * 获取收藏课程状态
     *
     * @param courseId 课程id
     * @param userId   用户id
     * @return true:已收藏 false:未收藏
     */
    Boolean getCourseFavorite(Long courseId, Long userId);

    /**
     * 查询用户收藏课程列表
     *
     * @param userId 用户id
     * @return 用户收藏课程列表
     */
    List<CourseBase> getCourseFavoriteList(Long userId);
}
