package com.jwb.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.model.dto.QueryCommentDto;
import com.jwb.content.model.po.CourseComment;

/**
 * @author zss
 * @description 针对表【course_comment】的数据库操作Service
 * @createDate 2024-08-03 15:59:20
 */
public interface CourseCommentService extends IService<CourseComment> {

    /**
     * 添加评论
     *
     * @param courseComment 评论信息
     * @return 评论信息
     */
    CourseComment addComment(CourseComment courseComment);


    /**
     * 获取指定课程评论
     *
     * @param pageParams      分页参数
     * @param queryCommentDto 查询条件
     * @return 评论列表
     */
    PageResult<CourseComment> getComment(PageParams pageParams, QueryCommentDto queryCommentDto);

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return 是否删除成功
     */
    Boolean deleteComment(Long id);
}
