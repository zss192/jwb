package com.jwb.content.model.dto;

import com.jwb.content.model.po.JwbTeacher;
import lombok.Data;

import java.util.List;

@Data
public class CoursePreviewDto {

    /**
     * 课程基本计划、课程营销信息
     */
    CourseBaseInfoDto courseBase;

    /**
     * 课程计划信息
     */
    List<TeachplanDto> teachplans;

    /**
     * 师资信息
     */
    JwbTeacher teacher;
}
