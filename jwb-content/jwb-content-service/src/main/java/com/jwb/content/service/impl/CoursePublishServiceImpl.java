package com.jwb.content.service.impl;

import com.jwb.content.model.dto.CourseBaseInfoDto;
import com.jwb.content.model.dto.CoursePreviewDto;
import com.jwb.content.model.dto.TeachplanDto;
import com.jwb.content.service.CourseBaseService;
import com.jwb.content.service.CoursePublishService;
import com.jwb.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private CourseBaseService courseBaseService;
    @Autowired
    private TeachplanService teachplanService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 根据课程id查询 课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        // 根据课程id，查询课程计划
        List<TeachplanDto> teachplanDtos = teachplanService.findTeachplanTree(courseId);
        // 封装返回
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanDtos);
        return coursePreviewDto;
    }
}
