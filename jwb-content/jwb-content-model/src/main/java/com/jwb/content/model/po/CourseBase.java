package com.jwb.content.model.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程基本信息(CourseBase)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:29
 */
@Setter
@Getter
public class CourseBase implements Serializable {
    private static final long serialVersionUID = 307434593195492524L;
    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 课程图片
     */
    private String charge;
    /**
     * 学习人数
     */
    private Long studyCount;
    /**
     * 收藏人数
     */
    private Long favoriteCount;
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


}

