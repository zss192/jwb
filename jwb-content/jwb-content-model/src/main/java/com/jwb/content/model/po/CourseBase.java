package com.jwb.content.model.po;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程基本信息(CourseBase)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:29
 */
public class CourseBase implements Serializable {
    private static final long serialVersionUID = 307434593195492524L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 机构ID
     */
    private Long companyId;
    /**
     * 机构名称
     */
    private String companyName;
    /**
     * 课程名称
     */
    private String name;
    /**
     * 适用人群
     */
    private String users;
    /**
     * 课程标签
     */
    private String tags;
    /**
     * 大分类
     */
    private String mt;
    /**
     * 小分类
     */
    private String st;
    /**
     * 课程等级
     */
    private String grade;
    /**
     * 教育模式(common普通，record 录播，live直播等）
     */
    private String teachmode;
    /**
     * 课程介绍
     */
    private String description;
    /**
     * 课程图片
     */
    private String pic;
    /**
     * 创建时间
     */
    private LocalDateTime createDate;
    /**
     * 修改时间
     */
    private LocalDateTime changeDate;
    /**
     * 创建人
     */
    private String createPeople;
    /**
     * 更新人
     */
    private String changePeople;
    /**
     * 审核状态
     */
    private String auditStatus;
    /**
     * 课程发布状态 未发布  已发布 下线
     */
    private String status;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getMt() {
        return mt;
    }

    public void setMt(String mt) {
        this.mt = mt;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTeachmode() {
        return teachmode;
    }

    public void setTeachmode(String teachmode) {
        this.teachmode = teachmode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
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

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

