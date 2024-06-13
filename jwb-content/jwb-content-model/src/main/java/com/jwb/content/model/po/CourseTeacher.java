package com.jwb.content.model.po;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程-教师关系表(CourseTeacher)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
@Data
public class CourseTeacher implements Serializable {
    private static final long serialVersionUID = -13892189285468110L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 课程标识
     */
    private Long courseId;
    /**
     * 教师标识
     */
    private String teacherName;
    /**
     * 教师职位
     */
    private String position;
    /**
     * 教师简介
     */
    private String introduction;
    /**
     * 照片
     */
    private String photograph;
    /**
     * 创建时间
     */
    private LocalDateTime createDate;
}

