package com.jwb.content.model.po;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程-教师关系表(CourseTeacher)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
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
    private Date createDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getPhotograph() {
        return photograph;
    }

    public void setPhotograph(String photograph) {
        this.photograph = photograph;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

}

