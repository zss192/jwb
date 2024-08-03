package com.jwb.content.api;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.model.dto.QueryCommentDto;
import com.jwb.content.model.po.CourseComment;
import com.jwb.content.service.CourseCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "课程评论接口", tags = "课程评论接口")
@RequestMapping("/course-comment")
public class CourseCommentController {
    @Autowired
    private CourseCommentService courseCommentService;

    @ApiOperation("课程评论")
    @PostMapping("/addComment")
    public CourseComment addComment(@RequestBody CourseComment courseComment) {
        return courseCommentService.addComment(courseComment);
    }

    @ApiOperation("获取课程评论")
    @PostMapping("/getComment")
    public PageResult<CourseComment> getComment(PageParams pageParams, @RequestBody QueryCommentDto queryCommentDto) {
        return courseCommentService.getComment(pageParams, queryCommentDto);
    }

    @ApiOperation("删除课程评论")
    @DeleteMapping("/deleteComment/{id}")
    public Boolean deleteComment(@PathVariable Long id) {
        return courseCommentService.deleteComment(id);
    }
}
