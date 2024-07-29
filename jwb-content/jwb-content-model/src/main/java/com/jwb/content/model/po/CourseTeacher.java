package com.jwb.content.model.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程-教师关系表
 *
 * @TableName course_teacher
 */

@Data
public class CourseTeacher implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 教师id
     */
    private Long teacherId;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     *
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}