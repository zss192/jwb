package com.jwb.company.controller;

import com.jwb.company.model.po.JwbTeacher;
import com.jwb.company.service.JwbTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Api(value = "教师管理接口", tags = "教师管理接口")
public class TeacherController {

    @Autowired
    JwbTeacherService jwbTeacherService;

    @ApiOperation(value = "添加教师", tags = "添加教师")
    @PostMapping("/addTeacher")
    public JwbTeacher addTeacher(@RequestBody JwbTeacher jwbTeacher) {
        return jwbTeacherService.addTeacher(jwbTeacher);
    }

    @ApiOperation(value = "查询教师", tags = "查询教师")
    @GetMapping("/teacher/{id}")
    public JwbTeacher getTeacher(@PathVariable String id) {
        return jwbTeacherService.getTeacher(id);
    }

    @ApiOperation(value = "查询所有教师", tags = "查询所有教师")
    @GetMapping("/teacher/list")
    public List<JwbTeacher> getTeacherList() {
        return jwbTeacherService.getTeacherList();
    }

    @ApiOperation(value = "查询教师排行榜", tags = "查询教师排行榜")
    @GetMapping("/teacher/rank")
    public List<JwbTeacher> getTeacherRank(Long count) {
        return jwbTeacherService.getTeacherRank(count);
    }
}
