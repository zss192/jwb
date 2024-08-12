package com.jwb.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.content.model.po.CourseBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zss
 * @description 针对表【course_base(课程基本信息)】的数据库操作Mapper
 * @createDate 2024-06-11 15:29:16
 * @Entity generator.domain.CourseBase
 */

@Mapper
public interface CourseBaseMapper extends BaseMapper<CourseBase> {

    /**
     * 获取全部数据的id
     *
     * @return 全部数据的id
     */
    @Select("select id from course_base")
    List<String> getIds();
}




