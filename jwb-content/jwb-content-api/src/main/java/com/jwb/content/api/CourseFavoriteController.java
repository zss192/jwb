package com.jwb.content.api;

import com.jwb.content.model.po.CourseBase;
import com.jwb.content.service.CourseFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "课程收藏管理接口", tags = "课程收藏管理接口")
@RestController
public class CourseFavoriteController {

    @Autowired
    private CourseFavoriteService courseFavoriteService;

    @ApiOperation("获取收藏课程状态")
    @GetMapping("/course/favorite")
    public Boolean getCourseFavorite(Long courseId, Long userId) {
        return courseFavoriteService.getCourseFavorite(courseId, userId);
    }

    @ApiOperation("收藏课程")
    @PutMapping("/course/favorite")
    public void courseFavorite(Long courseId, Long userId, Boolean isFavorite) {
        courseFavoriteService.courseFavorite(courseId, userId, isFavorite);
    }

    @ApiOperation("查询用户收藏课程列表")
    @GetMapping("/course/favorite/list")
    public List<CourseBase> getCourseFavoriteList(Long userId) {
        return courseFavoriteService.getCourseFavoriteList(userId);
    }
}
