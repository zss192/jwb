package com.jwb.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.base.exception.JwbException;
import com.jwb.content.mapper.CourseBaseMapper;
import com.jwb.content.mapper.CourseFavoriteMapper;
import com.jwb.content.model.dto.CourseFavoriteDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseFavorite;
import com.jwb.content.service.CourseFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zss
 * @description 针对表【course_favorite】的数据库操作Service实现
 * @createDate 2024-07-31 16:59:44
 */
@Service
public class CourseFavoriteServiceImpl extends ServiceImpl<CourseFavoriteMapper, CourseFavorite>
        implements CourseFavoriteService {

    @Autowired
    private CourseFavoriteMapper courseFavoriteMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 课程收藏
     *
     * @param courseId   课程id
     * @param userId     用户id
     * @param isFavorite true:收藏 false:取消收藏
     */
    @Transactional
    @Override
    public void courseFavorite(Long courseId, Long userId, Boolean isFavorite) {
        LambdaQueryWrapper<CourseFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseFavorite::getCourseId, courseId);
        queryWrapper.eq(CourseFavorite::getUserId, userId);
        CourseFavorite courseFavorite = courseFavoriteMapper.selectOne(queryWrapper);

        if (isFavorite) {
            addFavorite(courseId, userId, courseFavorite);
        } else {
            removeFavorite(courseId, courseFavorite);
        }
        // 信息同步更新到Redis 先更新后删除
        String cacheKey = "course_dynamic:" + courseId;
        redisTemplate.delete(cacheKey);
    }

    private void addFavorite(Long courseId, Long userId, CourseFavorite courseFavorite) {
        if (courseFavorite != null) {
            JwbException.cast("已收藏，无法重复收藏");
            return;
        }
        courseFavorite = new CourseFavorite();
        courseFavorite.setCourseId(courseId);
        courseFavorite.setUserId(userId);
        courseFavorite.setCreateTime(LocalDateTime.now());
        courseFavoriteMapper.insert(courseFavorite);
        updateFavoriteCount(courseId, 1);
    }

    private void removeFavorite(Long courseId, CourseFavorite courseFavorite) {
        if (courseFavorite == null) {
            JwbException.cast("未收藏，无法取消收藏");
            return;
        }
        courseFavoriteMapper.deleteById(courseFavorite);
        updateFavoriteCount(courseId, -1);
    }

    private void updateFavoriteCount(Long courseId, int count) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setFavoriteCount(courseBase.getFavoriteCount() + count);
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 获取收藏课程状态
     *
     * @param courseId 课程id
     * @param userId   用户id
     * @return true:已收藏 false:未收藏
     */
    @Override
    public Boolean getCourseFavorite(Long courseId, Long userId) {
        LambdaQueryWrapper<CourseFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseFavorite::getCourseId, courseId);
        queryWrapper.eq(CourseFavorite::getUserId, userId);
        return courseFavoriteMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 查询用户收藏课程列表
     *
     * @param userId 用户id
     * @return 用户收藏课程列表
     */
    @Override
    public List<CourseFavoriteDto> getCourseFavoriteList(Long userId) {
        return courseFavoriteMapper.getCourseFavoriteList(userId);
    }
}




