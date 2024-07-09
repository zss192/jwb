package com.jwb.content.service;

import com.jwb.content.model.dto.CoursePreviewDto;
import com.jwb.content.model.po.CoursePublish;

import java.io.File;

/**
 * 课程预览、发布接口
 */
public interface CoursePublishService {
    /**
     * 根据课程id获取课程预览信息
     *
     * @param courseId 课程id
     * @return package com.jwb.content.model.dto.CoursePreviewDto;
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param courseId 课程id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 发布课程
     *
     * @param companyId
     * @param courseId
     */
    void publishCourse(Long companyId, Long courseId);

    /**
     * 课程静态化
     *
     * @param courseId 课程id
     * @return 静态化文件
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param courseId 课程id
     * @param file     静态化文件
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 保存课程索引
     *
     * @param courseId 课程id
     */
    Boolean saveCourseIndex(Long courseId);

    /**
     * 根据id获取课程发布信息
     *
     * @param courseId
     */
    CoursePublish getCoursePublish(Long courseId);
}
