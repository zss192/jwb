package com.jwb.company.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.company.model.po.JwbTeacher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zss
 * @description 针对表【jwb_teacher】的数据库操作Mapper
 * @createDate 2024-07-28 18:52:25
 * @Entity com.jwb.company.JwbTeacher
 */
public interface JwbTeacherMapper extends BaseMapper<JwbTeacher> {

    List<JwbTeacher> selectByIds(ArrayList<Long> ids);
}




