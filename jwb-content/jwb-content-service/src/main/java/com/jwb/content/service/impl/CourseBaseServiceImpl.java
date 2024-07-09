package com.jwb.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.mapper.*;
import com.jwb.content.model.dto.AddCourseDto;
import com.jwb.content.model.dto.CourseBaseInfoDto;
import com.jwb.content.model.dto.EditCourseDto;
import com.jwb.content.model.dto.QueryCourseParamsDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseMarket;
import com.jwb.content.model.po.CourseTeacher;
import com.jwb.content.model.po.Teachplan;
import com.jwb.content.service.CourseBaseService;
import com.jwb.content.service.CourseMarketService;
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
    @Autowired
    CourseMarketService courseMarketService;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据机构id查询
        queryWrapper.eq(CourseBase::getCompanyId, companyId);
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
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        // 查询小分类名称
        String StName = courseCategoryMapper.selectById(courseBase.getSt()).getName();
        courseBaseInfoDto.setStName(StName);
        // 查询大分类名称
        String MtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        courseBaseInfoDto.setMtName(MtName);
        return courseBaseInfoDto;
    }

    // 保存营销信息
    private void saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge)) {
            JwbException.cast("收费规则为空");
        }
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                JwbException.cast("课程设置了收费，价格不能为空，且必须大于0");
            }
        }
        Long id = courseMarket.getId();
        CourseMarket courseMarketQuery = courseMarketMapper.selectById(id);
        if (courseMarketQuery == null) {
            courseMarketMapper.insert(courseMarket);
        } else {
            courseMarketMapper.updateById(courseMarketQuery);
        }
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        // 判断当前修改课程是否属于当前机构
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId())) {
            JwbException.cast("只允许修改本机构的课程");
        }
        // 拷贝对象
        BeanUtils.copyProperties(editCourseDto, courseBase);
        // 更新，设置更新时间
        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);
        // 查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 由于课程营销信息不是必填项，故这里先判断一下
        if (courseMarket == null) {
            courseMarket = new CourseMarket();
        }
        courseMarket.setId(courseId);
        // 获取课程收费状态并设置
        String charge = editCourseDto.getCharge();
        courseMarket.setCharge(charge);
        // 如果课程收费，则判断价格是否正常
        if (charge.equals("201001")) {
            Float price = editCourseDto.getPrice();
            if (price == null || price <= 0) {
                JwbException.cast("课程设置了收费，价格不能为空，且必须大于0");
            }
        }
        // 对象拷贝
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        // 有则更新，无则插入
        if (editCourseDto.getPrice() == null) {
            courseMarket.setPrice(0F);
        }
        if (editCourseDto.getOriginalPrice() == null) {
            courseMarket.setOriginalPrice(0F);
        }
        courseMarketService.saveOrUpdate(courseMarket);
        return getCourseBaseInfo(courseId);
    }

    @Transactional
    @Override
    public void delectCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId()))
            JwbException.cast("只允许删除本机构的课程");
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        // 删除营销信息
        courseMarketMapper.deleteById(courseId);
        // 删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }
}



