package com.jwb.content.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName course_comment
 */
@TableName(value = "course_comment")
@Data
public class CourseComment implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 评论内容
     */
    private String commentText;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户评分 0-5
     */
    private Double starRank;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户头像
     */
    private String userHead;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}