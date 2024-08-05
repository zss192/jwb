package com.jwb.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.content.model.po.CoursePublish;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zss
 * @description 针对表【course_publish(课程发布)】的数据库操作Mapper
 * @createDate 2024-07-01 17:20:30
 * @Entity com.jwb.content.CoursePublish
 */
public interface CoursePublishMapper extends BaseMapper<CoursePublish> {

    List<CoursePublish> selectByIds(ArrayList<Long> courseIds);
}




