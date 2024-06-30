package com.jwb.content.model.po;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (TeachplanMedia)实体类
 *
 * @author makejava
 * @since 2024-06-10 18:41:30
 */
@Setter
@Getter
public class TeachplanMedia implements Serializable {
    private static final long serialVersionUID = 201888176289630632L;
    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
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

    private LocalDateTime createDate;
    /**
     * 创建人
     */
    private String createPeople;
    /**
     * 修改人
     */
    private String changePeople;


}

