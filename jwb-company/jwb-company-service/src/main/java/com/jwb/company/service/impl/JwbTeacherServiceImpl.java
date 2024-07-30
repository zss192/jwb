package com.jwb.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.company.mapper.JwbTeacherMapper;
import com.jwb.company.model.po.JwbTeacher;
import com.jwb.company.service.JwbTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zss
 * @description 针对表【jwb_teacher】的数据库操作Service实现
 * @createDate 2024-07-28 18:52:25
 */
@Service
public class JwbTeacherServiceImpl extends ServiceImpl<JwbTeacherMapper, JwbTeacher>
        implements JwbTeacherService {

    @Autowired
    JwbTeacherMapper jwbTeacherMapper;

    @Transactional
    @Override
    public JwbTeacher addTeacher(JwbTeacher jwbTeacher) {
        String id = jwbTeacher.getId();
        if (id == null) {
            // 新增教师
            jwbTeacher.setCreateTime(LocalDateTime.now());
            jwbTeacher.setUpdateTime(LocalDateTime.now());
            jwbTeacherMapper.insert(jwbTeacher);
        } else {
            // 修改教师
            jwbTeacher.setUpdateTime(LocalDateTime.now());
            jwbTeacherMapper.updateById(jwbTeacher);
        }
        return jwbTeacher;
    }

    @Override
    public JwbTeacher getTeacher(String id) {
        return jwbTeacherMapper.selectById(id);
    }

    @Override
    public List<JwbTeacher> getTeacherList() {
        return jwbTeacherMapper.selectList(null);
    }

    /**
     * 查询教师排行榜
     *
     * @return
     */
    @Override
    public List<JwbTeacher> getTeacherRank(Long count) {
        QueryWrapper<JwbTeacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("count");
        queryWrapper.last("LIMIT " + count);
        return jwbTeacherMapper.selectList(queryWrapper);
    }
}




