package com.jwb.model.po;

import java.io.Serializable;
import java.util.Date;

/**
 * (TeachplanMedia)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
public class TeachplanMedia implements Serializable {
    private static final long serialVersionUID = 201888176289630632L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 媒资文件id
     */
    private String mediaId;
    /**
     * 课程计划标识
     */
    private Long teachplanId;
    /**
     * 课程标识
     */
    private Long courseId;
    /**
     * 媒资文件原始名称
     */
    private String mediaFilename;

    private Date createDate;
    /**
     * 创建人
     */
    private String createPeople;
    /**
     * 修改人
     */
    private String changePeople;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public Long getTeachplanId() {
        return teachplanId;
    }

    public void setTeachplanId(Long teachplanId) {
        this.teachplanId = teachplanId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getMediaFilename() {
        return mediaFilename;
    }

    public void setMediaFilename(String mediaFilename) {
        this.mediaFilename = mediaFilename;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreatePeople() {
        return createPeople;
    }

    public void setCreatePeople(String createPeople) {
        this.createPeople = createPeople;
    }

    public String getChangePeople() {
        return changePeople;
    }

    public void setChangePeople(String changePeople) {
        this.changePeople = changePeople;
    }

}

