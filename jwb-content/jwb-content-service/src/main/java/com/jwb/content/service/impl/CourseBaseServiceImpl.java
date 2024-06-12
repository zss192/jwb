package com.jwb.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.mapper.CourseBaseMapper;
import com.jwb.content.mapper.CourseCategoryMapper;
import com.jwb.content.mapper.CourseMarketMapper;
import com.jwb.content.model.dto.AddCourseDto;
import com.jwb.content.model.dto.CourseBaseInfoDto;
import com.jwb.content.model.dto.QueryCourseParamsDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseMarket;
import com.jwb.content.service.CourseBaseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zss
 * @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
 * @createDate 2024-06-11 16:24:40
 */
@Service
public class CourseBaseServiceImpl implements CourseBaseService {
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        //TODO:根据课程发布状态查询

        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    /**
     * @param companyId
     * @param addCourseDto
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");

        int insert = courseBaseMapper.insert(courseBase);
        if (insert < 0) {
            throw new RuntimeException("添加课程失败");
        }
        // 向课程营销表course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        Long courseId = courseBase.getId();
        courseMarket.setId(courseId);
        saveCourseMarket(courseMarket);
        
        return getCourseBaseInfo(courseId);
    }

    // 查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        // 查询小分类名称
        String StName = courseCategoryMapper.selectById(courseBase.getSt()).getName();
        courseBaseInfoDto.setStName(StName);
        // 查询大分类名称
        String MtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        courseBaseInfoDto.setMtName(MtName);
        return courseBaseInfoDto;
    }

    // 保存营销信息
    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge)) {
            throw new RuntimeException("收费规则为空");
        }
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                throw new RuntimeException("课程的价格不能为空且必须大于0");
            }
        }
        Long id = courseMarket.getId();
        CourseMarket courseMarketQuery = courseMarketMapper.selectById(id);
        if (courseMarketQuery == null) {
            return courseMarketMapper.insert(courseMarket);
        } else {
            return courseMarketMapper.updateById(courseMarketQuery);
        }
    }
}



