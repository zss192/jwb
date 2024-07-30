package com.jwb.search.service;

import com.jwb.base.model.PageParams;
import com.jwb.search.dto.SearchCourseParamDto;
import com.jwb.search.dto.SearchPageResultDto;
import com.jwb.search.po.CourseIndex;

/**
 * @description 课程搜索service
 */
public interface CourseSearchService {


    /**
     * @param pageParams           分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.jwb.base.model.PageResult<com.jwb.search.po.CourseIndex> 课程列表
     * @description 搜索课程列表
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

}
