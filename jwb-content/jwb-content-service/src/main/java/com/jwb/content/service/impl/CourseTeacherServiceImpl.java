package com.jwb.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.base.exception.JwbException;
import com.jwb.content.feignclient.TeacherServiceClient;
import com.jwb.content.mapper.CourseTeacherMapper;
import com.jwb.content.model.po.CourseTeacher;
import com.jwb.content.model.po.JwbTeacher;
import com.jwb.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    TeacherServiceClient teacherServiceClient;


    @Override
    public JwbTeacher getCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        CourseTeacher courseTeacher = courseTeacherMapper.selectOne(queryWrapper);
        if (courseTeacher == null) {
            return null;
        }
        return teacherServiceClient.getTeacher(String.valueOf(courseTeacher.getTeacherId()));
    }


    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getTeacherId, teacherId);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        int flag = courseTeacherMapper.delete(queryWrapper);
        if (flag < 0)
            JwbException.cast("删除失败");
    }

    @Override
    public void updateTeacher(Long courseBaseId, Long courseTeacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseBaseId);
        CourseTeacher courseTeacher = courseTeacherMapper.selectOne(queryWrapper);
        // 根据教师id查询教师信息
        JwbTeacher teacher = teacherServiceClient.getTeacher(String.valueOf(courseTeacherId));
        String teacherName = teacher.getTeacherName();
        // 没有就插入,有就更新
        if (courseTeacher == null) {
            CourseTeacher newCourseTeacher = new CourseTeacher();
            newCourseTeacher.setTeacherId(courseTeacherId);
            newCourseTeacher.setTeacherName(teacherName);
            newCourseTeacher.setCourseId(courseBaseId);
            newCourseTeacher.setCreateDate(LocalDateTime.now());
            newCourseTeacher.setUpdateTime(LocalDateTime.now());
            courseTeacherMapper.insert(newCourseTeacher);
        } else {
            courseTeacher.setTeacherId(courseTeacherId);
            courseTeacher.setTeacherName(teacherName);
            courseTeacher.setUpdateTime(LocalDateTime.now());
            courseTeacherMapper.updateById(courseTeacher);
        }
    }

    /**
     * 批量查询教师信息
     *
     * @param courseIds 课程id列表
     * @return 课程id与教师信息的映射
     */
    @Override
    public Map<Long, CourseTeacher> getCourseTeacherBatch(ArrayList<Long> courseIds) {
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(new LambdaQueryWrapper<CourseTeacher>().in(CourseTeacher::getCourseId, courseIds));
        Map<Long, CourseTeacher> courseTeacherMap = new HashMap<>();
        for (CourseTeacher courseTeacher : courseTeachers) {
            courseTeacherMap.put(courseTeacher.getCourseId(), courseTeacher);
        }

        return courseTeacherMap;
    }

    public CourseTeacher getCourseTeacher(CourseTeacher courseTeacher) {
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }
}
