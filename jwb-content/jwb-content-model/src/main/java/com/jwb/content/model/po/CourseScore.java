package com.jwb.content.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName course_score
 */
@TableName(value = "course_score")
@Data
public class CourseScore implements Serializable {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 平均分数
     */
    private Double avgScore;

    /**
     * 五星分数占比
     */
    private Long fiveScore;

    /**
     * 四星分数占比
     */
    private Long fourScore;

    /**
     * 三星分数占比
     */
    private Long threeScore;

    /**
     * 二星分数占比
     */
    private Long twoScore;

    /**
     * 一星分数占比
     */
    private Long oneScore;

    /**
     * 总评分
     */
    private Double sumScore;

    /**
     * 评论人数
     */
    private Long peopleCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}