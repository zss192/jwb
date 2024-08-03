package com.jwb.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.mapper.CourseBaseMapper;
import com.jwb.content.mapper.CourseCommentMapper;
import com.jwb.content.model.dto.QueryCommentDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseComment;
import com.jwb.content.service.CourseCommentService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zss
 * @description 针对表【course_comment】的数据库操作Service实现
 * @createDate 2024-08-03 15:59:20
 */
@Service
public class CourseCommentServiceImpl extends ServiceImpl<CourseCommentMapper, CourseComment>
        implements CourseCommentService {

    @Autowired
    private CourseCommentMapper courseCommentMapper;
    @Autowired
    private CourseBaseMapper CourseBaseMapper;

    /**
     * 添加评论
     *
     * @param courseComment 评论信息
     * @return 评论信息
     */
    @Override
    @Transactional
    public CourseComment addComment(CourseComment courseComment) {
        // 根据用户id和课程id查询是否已经评论过
        LambdaQueryWrapper<CourseComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseComment::getCourseId, courseComment.getCourseId());
        queryWrapper.eq(CourseComment::getUserId, courseComment.getUserId());
        CourseComment comment = courseCommentMapper.selectOne(queryWrapper);
        if (comment != null) {
            JwbException.cast("您已经评论过该课程");
        }
        CourseBase courseBase = CourseBaseMapper.selectById(courseComment.getCourseId());
        String CourseName = courseBase.getName();
        courseComment.setCourseName(CourseName);
        courseComment.setCreateTime(LocalDateTime.now());
        courseCommentMapper.insert(courseComment);
        return courseComment;
    }

    /**
     * 获取指定课程评论
     *
     * @param pageParams      分页参数
     * @param queryCommentDto 查询条件
     * @return 评论列表
     */
    @Override
    public PageResult<CourseComment> getComment(PageParams pageParams, QueryCommentDto queryCommentDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(queryCommentDto.getCourseId()), CourseComment::getCourseId, queryCommentDto.getCourseId());
        queryWrapper.like(StringUtils.isNotEmpty(queryCommentDto.getCourseName()), CourseComment::getCourseName, queryCommentDto.getCourseName());
        //分页对象
        Page<CourseComment> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseComment> pageResult = courseCommentMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseComment> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteComment(Long id) {
        return courseCommentMapper.deleteById(id) > 0;
    }
}




