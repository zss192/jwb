package com.jwb.company.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName jwb_teacher
 */
@TableName(value = "jwb_teacher")
@Data
public class JwbTeacher implements Serializable {
    /**
     *
     */
    @TableId(value = "id")
    private String id;

    /**
     * 姓名
     */
    @TableField(value = "teacher_name")
    private String teacherName;

    /**
     * 个人简介
     */
    @TableField(value = "introduction")
    private String introduction;

    /**
     * 老师照片
     */
    @TableField(value = "photograph")
    private String photograph;

    /**
     * 学习过的人数
     */
    @TableField(value = "count")
    private Long count;

    /**
     * 教师职位
     */
    @TableField(value = "position")
    private String position;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     *
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

}