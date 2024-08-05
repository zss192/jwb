package com.jwb.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.content.model.po.CourseScore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zss
 * @description 针对表【course_score】的数据库操作Mapper
 * @createDate 2024-08-03 19:03:06
 * @Entity com.jwb.content.CourseScore
 */
public interface CourseScoreMapper extends BaseMapper<CourseScore> {

    List<CourseScore> selectByIds(ArrayList<Long> courseIds);
}




