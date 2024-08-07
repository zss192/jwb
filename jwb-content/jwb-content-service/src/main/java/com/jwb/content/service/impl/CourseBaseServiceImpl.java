package com.jwb.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.mapper.*;
import com.jwb.content.model.dto.*;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseMarket;
import com.jwb.content.model.po.CourseTeacher;
import com.jwb.content.model.po.Teachplan;
import com.jwb.content.service.CourseBaseService;
import com.jwb.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author zss
 * @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
 * @createDate 2024-06-11 16:24:40
 */
@Service
@Slf4j
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
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

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
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        queryWrapper.orderByDesc(CourseBase::getChangeDate);
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
        courseBase.setChangeDate(LocalDateTime.now());
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
        courseBase.setAuditStatus("202002"); // 未提交
        courseBase.setStatus("203001"); // 未发布
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
        // 如果课程已发布还要删除elasticSearch中的课程信息
        if ("203002".equals(courseBase.getStatus())) {
            rabbitTemplate.convertAndSend("course.topic.exchange", "course.delete", courseId);
        }
    }

    @Override
    public void addStudyCount(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStudyCount(courseBase.getStudyCount() + 1);
        courseBaseMapper.updateById(courseBase);
        // 信息同步更新到Redis 先更新后删除
        String cacheKey = "course_dynamic:" + courseId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 获取课程动态信息
     *
     * @param courseId 课程id
     * @return 课程动态信息
     */
    @Override
    public CourseDynamicDto getCourseDynamicInfo(Long courseId) {
        String cacheKey = "course_dynamic:" + courseId;
        // 从缓存中查询
        String courseDynamicCacheJson = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotEmpty(courseDynamicCacheJson)) {
            log.debug("从缓存中查询");
            return "null".equals(courseDynamicCacheJson) ? null : JSON.parseObject(courseDynamicCacheJson, CourseDynamicDto.class);
        }

        // 加分布式锁防止缓存击穿和缓存雪崩
        RLock lock = redissonClient.getLock("courseDynamicQueryLock" + courseId);
        lock.lock();
        try {
            // 再次从缓存中查询，避免并发时重复查询数据库
            courseDynamicCacheJson = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotEmpty(courseDynamicCacheJson)) {
                log.debug("从缓存中查询");
                return "null".equals(courseDynamicCacheJson) ? null : JSON.parseObject(courseDynamicCacheJson, CourseDynamicDto.class);
            }

            log.debug("缓存中没有，查询数据库");
            CourseBase courseBase = courseBaseMapper.selectById(courseId);
            CourseDynamicDto courseDynamicDto = new CourseDynamicDto();
            if (courseBase == null) {
                // 缓存空值防止缓存穿透
                redisTemplate.opsForValue().set(cacheKey, "null", 5 + new Random().nextInt(10), TimeUnit.SECONDS);
                return null;
            } else {
                courseDynamicDto.setStudyCount(courseBase.getStudyCount());
                courseDynamicDto.setFavoriteCount(courseBase.getFavoriteCount());
            }

            // 缓存查询结果
            String jsonString = JSON.toJSONString(courseDynamicDto);
            // 过期时间加上一个随机值防止缓存雪崩
            redisTemplate.opsForValue().set(cacheKey, jsonString, 900 + new Random().nextInt(100), TimeUnit.SECONDS);
            return courseDynamicDto;
        } finally {
            lock.unlock();
        }
    }
}



