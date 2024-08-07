package com.jwb.company.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jwb.company.model.po.JwbTeacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zss
 * @description 针对表【jwb_teacher】的数据库操作Service
 * @createDate 2024-07-28 18:52:25
 */
public interface JwbTeacherService extends IService<JwbTeacher> {

    JwbTeacher addTeacher(JwbTeacher jwbTeacher);

    JwbTeacher getTeacher(String id);

    List<JwbTeacher> getTeacherList();

    List<JwbTeacher> getTeacherRank(Long count);

    /**
     * 批量查询教师
     *
     * @param ids id列表
     * @return 教师信息
     */
    Map<Long, JwbTeacher> getTeacherBatch(ArrayList<Long> ids);
}
