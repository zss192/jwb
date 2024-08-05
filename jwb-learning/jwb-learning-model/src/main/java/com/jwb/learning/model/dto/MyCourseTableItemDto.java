package com.jwb.learning.model.dto;

import com.jwb.learning.model.po.JwbCourseTables;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @description 我的课程查询条件
 */
@Data
@ToString
public class MyCourseTableItemDto extends JwbCourseTables {

    /**
     * 最近学习时间
     */
    private LocalDateTime learnDate;

    /**
     * 学习时长
     */
    private Long learnLength;

    /**
     * 章节id
     */
    private Long teachplanId;

    /**
     * 章节名称
     */
    private String teachplanName;

    /**
     * 课程封面
     */
    private String pic;

    /**
     * 课程教师
     */
    private String teacherName;
    /**
     * 课程评分
     */
    private Double avgScore;
}
