package com.jwb.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageResult;
import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.model.po.CourseScore;
import com.jwb.content.model.po.CourseTeacher;
import com.jwb.learning.feignclient.ContentServiceClient;
import com.jwb.learning.mapper.JwbChooseCourseMapper;
import com.jwb.learning.mapper.JwbCourseTablesMapper;
import com.jwb.learning.model.dto.JwbChooseCourseDto;
import com.jwb.learning.model.dto.JwbCourseTablesDto;
import com.jwb.learning.model.dto.MyCourseTableItemDto;
import com.jwb.learning.model.dto.MyCourseTableParams;
import com.jwb.learning.model.po.JwbChooseCourse;
import com.jwb.learning.model.po.JwbCourseTables;
import com.jwb.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MyCourseTablesServiceImpl implements MyCourseTablesService {
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    JwbChooseCourseMapper chooseCourseMapper;
    @Autowired
    JwbCourseTablesMapper courseTablesMapper;
    @Autowired
    @Qualifier("threadPoolExecutor")
    private ExecutorService threadPool;

    @Override
    @Transactional
    public JwbChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 1. 选课调用内容管理服务提供的查询课程接口，查询课程收费规则
        // 1.1 查询课程
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if (coursePublish == null) {
            JwbException.cast("课程不存在");
        }
        // 1.2 获取收费规则
        String charge = coursePublish.getCharge();
        JwbChooseCourse chooseCourse = null;
        if ("201000".equals(charge)) {
            // 2. 如果是免费课程，向选课记录表、我的课程表添加数据
            log.info("添加免费课程..");
            chooseCourse = addFreeCourse(userId, coursePublish);
            addCourseTables(chooseCourse);
        } else {
            // 3. 如果是收费课程，向选课记录表添加数据
            log.info("添加收费课程");
            chooseCourse = addChargeCourse(userId, coursePublish);
        }

        // 4. 获取学生的学习资格
        JwbCourseTablesDto courseTablesDto = getLearningStatus(userId, courseId);
        // 5. 封装返回值
        JwbChooseCourseDto chooseCourseDto = new JwbChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, chooseCourseDto);
        chooseCourseDto.setLearnStatus(courseTablesDto.learnStatus);
        return chooseCourseDto;
    }

    /**
     * 添加到我的课程表
     *
     * @param chooseCourse 选课记录
     */
    public JwbCourseTables addCourseTables(JwbChooseCourse chooseCourse) {
        String status = chooseCourse.getStatus();
        if (!"701001".equals(status)) {
            JwbException.cast("选课未成功，无法添加到课程表");
        }
        JwbCourseTables courseTables = getJwbCourseTables(chooseCourse.getUserId(), chooseCourse.getCourseId());
        if (courseTables != null) {
            return courseTables;
        }
        courseTables = new JwbCourseTables();
        BeanUtils.copyProperties(chooseCourse, courseTables);
        courseTables.setChooseCourseId(chooseCourse.getId());
        courseTables.setCourseType(chooseCourse.getOrderType());
        courseTables.setUpdateDate(LocalDateTime.now());
        int insert = courseTablesMapper.insert(courseTables);
        if (insert <= 0) {
            JwbException.cast("添加我的课程表失败");
        }
        // 课程学习人数加1
        contentServiceClient.addStudyCount(courseTables.getCourseId());
        return courseTables;
    }

    /**
     * 根据用户id和课程id查询我的课程表中的某一门课程
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 我的课程表中的课程
     */
    public JwbCourseTables getJwbCourseTables(String userId, Long courseId) {
        return courseTablesMapper.selectOne(new LambdaQueryWrapper<JwbCourseTables>()
                .eq(JwbCourseTables::getUserId, userId)
                .eq(JwbCourseTables::getCourseId, courseId));
    }

    /**
     * 将付费课程加入到选课记录表
     *
     * @param userId        用户id
     * @param coursePublish 课程发布信息
     * @return 选课记录
     */
    public JwbChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 1. 先判断是否已经存在对应的选课，因为数据库中没有约束，所以可能存在相同数据的选课
        LambdaQueryWrapper<JwbChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<JwbChooseCourse>()
                .eq(JwbChooseCourse::getUserId, userId)
                .eq(JwbChooseCourse::getCourseId, coursePublish.getId())
                .eq(JwbChooseCourse::getOrderType, "700002")  // 收费课程
                .eq(JwbChooseCourse::getStatus, "701002");// 待支付
        // 1.1 由于可能存在多条，所以这里用selectList
        List<JwbChooseCourse> chooseCourses = chooseCourseMapper.selectList(lambdaQueryWrapper);
        // 1.2 如果已经存在对应的选课数据，返回一条即可
        if (!chooseCourses.isEmpty()) {
            return chooseCourses.get(0);
        }
        // 2. 数据库中不存在数据，添加选课信息，对照着数据库中的属性挨个set即可
        JwbChooseCourse chooseCourse = new JwbChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700002");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701002");
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = chooseCourseMapper.insert(chooseCourse);
        if (insert <= 0) {
            JwbException.cast("添加选课记录失败");
        }
        return chooseCourse;
    }

    /**
     * 将免费课程加入到选课表
     *
     * @param userId        用户id
     * @param coursePublish 课程发布信息
     * @return 选课记录
     */
    public JwbChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {
        // 1. 先判断是否已经存在对应的选课，因为数据库中没有约束，所以可能存在相同数据的选课
        LambdaQueryWrapper<JwbChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<JwbChooseCourse>()
                .eq(JwbChooseCourse::getUserId, userId)
                .eq(JwbChooseCourse::getCourseId, coursePublish.getId())
                .eq(JwbChooseCourse::getOrderType, "700001")  // 免费课程
                .eq(JwbChooseCourse::getStatus, "701001");// 选课成功
        // 1.1 由于可能存在多条，所以这里用selectList
        List<JwbChooseCourse> chooseCourses = chooseCourseMapper.selectList(lambdaQueryWrapper);
        // 1.2 如果已经存在对应的选课数据，返回一条即可
        if (!chooseCourses.isEmpty()) {
            return chooseCourses.get(0);
        }
        // 2. 数据库中不存在数据，添加选课信息，对照着数据库中的属性挨个set即可
        JwbChooseCourse chooseCourse = new JwbChooseCourse();
        chooseCourse.setCourseId(coursePublish.getId());
        chooseCourse.setCourseName(coursePublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursePublish.getCompanyId());
        chooseCourse.setOrderType("700001");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(coursePublish.getPrice());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001");
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        chooseCourseMapper.insert(chooseCourse);
        return chooseCourse;
    }

    /**
     * 判断学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return 学习资格状态
     * 查询数据字典 [{"code":"702001","desc":"正常学习"}
     * {"code":"702002","desc":"没有选课或选课后没有支付"}
     * {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    @Override
    public JwbCourseTablesDto getLearningStatus(String userId, Long courseId) {
        JwbCourseTablesDto courseTablesDto = new JwbCourseTablesDto();
        // 1. 查询我的课程表
        JwbCourseTables courseTables = getJwbCourseTables(userId, courseId);
        // 2. 未查到，返回一个状态码为"702002"的对象
        if (courseTables == null) {
            courseTablesDto = new JwbCourseTablesDto();
            courseTablesDto.setLearnStatus("702002");
            return courseTablesDto;
        }
        // 3. 查到了，判断是否过期
        boolean isExpires = LocalDateTime.now().isAfter(courseTables.getValidtimeEnd());
        BeanUtils.copyProperties(courseTables, courseTablesDto);
        if (isExpires) {
            // 3.1 已过期，返回状态码为"702003"的对象
            courseTablesDto.setLearnStatus("702003");
        } else {
            // 3.2 未过期，返回状态码为"702001"的对象
            courseTablesDto.setLearnStatus("702001");
        }
        return courseTablesDto;
    }

    /**
     * 根据选课id更新选课表支付状态为已支付并插入选课表
     *
     * @param chooseCourseId
     */
    @Override
    @Transactional
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        // 1. 根据选课id，查询选课表
        JwbChooseCourse chooseCourse = chooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse == null) {
            log.error("接收到购买课程的消息，根据选课id未查询到课程，选课id：{}", chooseCourseId);
            return false;
        }
        // 2. 选课状态为未支付时，更新选课状态为选课成功
        if ("701002".equals(chooseCourse.getStatus())) {
            chooseCourse.setStatus("701001");
            int update = chooseCourseMapper.updateById(chooseCourse);
            if (update <= 0) {
                log.error("更新选课记录失败：{}", chooseCourse);
            }
        }
        // 3. 向我的课程表添加记录
        addCourseTables(chooseCourse);
        return true;
    }

    /**
     * 分页查询我的课程表
     * <p>
     * 使用 CompletableFuture 版本
     * <p>
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    @Override
    public PageResult<MyCourseTableItemDto> myCourseTables(MyCourseTableParams params) {
        // 查询课程表
        Page<JwbCourseTables> pageResult = queryCourseTables(params);

        // 获取课程ID列表
        List<Long> courseIds = pageResult.getRecords().stream()
                .map(JwbCourseTables::getCourseId)
                .collect(Collectors.toList());

        // 查询课程发布表、课程评分表和课程教师表，使用自定义线程池，并返回 CompletableFuture
        CompletableFuture<Map<Long, CoursePublish>> coursePublishFuture = CompletableFuture.supplyAsync(
                () -> contentServiceClient.getCoursePublishBatch(new ArrayList<>(courseIds)), threadPool);
        CompletableFuture<Map<Long, CourseScore>> courseScoreFuture = CompletableFuture.supplyAsync(
                () -> contentServiceClient.getCourseScoreBatch(new ArrayList<>(courseIds)), threadPool);
        CompletableFuture<Map<Long, CourseTeacher>> courseTeacherFuture = CompletableFuture.supplyAsync(
                () -> contentServiceClient.getCourseTeacherBatch(new ArrayList<>(courseIds)), threadPool);

        // 等待所有 CompletableFuture 完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(coursePublishFuture, courseScoreFuture, courseTeacherFuture);

        // 等待所有任务完成，超时时间为5秒
        try {
            allFutures.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            JwbException.cast("查询我的课程表超时");
        }

        // 获取查询结果并构建DTO列表
        Map<Long, CoursePublish> coursePublishMap = coursePublishFuture.join();
        Map<Long, CourseScore> courseScoreMap = courseScoreFuture.join();
        Map<Long, CourseTeacher> courseTeacherMap = courseTeacherFuture.join();

        List<MyCourseTableItemDto> dtoRecords = buildDtoRecords(pageResult.getRecords(), coursePublishMap, courseScoreMap, courseTeacherMap);
        long total = pageResult.getTotal();
        return new PageResult<>(dtoRecords, total, params.getPage(), params.getSize());
    }

    // 查询课程表
    private Page<JwbCourseTables> queryCourseTables(MyCourseTableParams params) {
        int pageNo = params.getPage();
        long pageSize = params.getSize();
        String userId = params.getUserId();

        Page<JwbCourseTables> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<JwbCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JwbCourseTables::getUserId, userId);
        if (StringUtils.isNotEmpty(params.getCourseType())) {
            queryWrapper.eq(JwbCourseTables::getCourseType, params.getCourseType());
        }
        if (StringUtils.isNotEmpty(params.getExpiresType())) {
            if ("1".equals(params.getExpiresType())) {
                queryWrapper.between(JwbCourseTables::getValidtimeEnd, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
            } else if ("2".equals(params.getExpiresType())) {
                queryWrapper.lt(JwbCourseTables::getValidtimeEnd, LocalDateTime.now());
            }
        }
        queryWrapper.orderByDesc(JwbCourseTables::getCreateDate);
        return courseTablesMapper.selectPage(page, queryWrapper);
    }

    // 构建DTO列表
    private List<MyCourseTableItemDto> buildDtoRecords(List<JwbCourseTables> records, Map<Long, CoursePublish> coursePublishMap,
                                                       Map<Long, CourseScore> courseScoreMap, Map<Long, CourseTeacher> courseTeacherMap) {
        return records.stream().map(record -> {
            Long courseId = record.getCourseId();
            MyCourseTableItemDto dto = new MyCourseTableItemDto();
            BeanUtils.copyProperties(record, dto);
            dto.setPic(coursePublishMap.getOrDefault(courseId, new CoursePublish()).getPic());
            dto.setAvgScore(courseScoreMap.getOrDefault(courseId, new CourseScore()).getAvgScore());
            dto.setTeacherName(courseTeacherMap.getOrDefault(courseId, new CourseTeacher()).getTeacherName());
            return dto;
        }).collect(Collectors.toList());
    }


    /**
     * 分页查询我的课程表
     * <p>
     * 不使用 CompletableFuture版本（用作查询对比）
     * <p>
     *
     * @param params 查询参数
     * @return 分页查询结果
     */
    public PageResult<MyCourseTableItemDto> myCourseTablesOld(MyCourseTableParams params) {
        Page<JwbCourseTables> pageResult = queryCourseTables(params);
        List<Long> courseIds = pageResult.getRecords().stream()
                .map(JwbCourseTables::getCourseId)
                .collect(Collectors.toList());

        // 批量查询课程信息，这里用的串行查询
        Map<Long, CoursePublish> coursePublishMap = contentServiceClient.getCoursePublishBatch(new ArrayList<>(courseIds));
        Map<Long, CourseScore> courseScoreMap = contentServiceClient.getCourseScoreBatch(new ArrayList<>(courseIds));
        Map<Long, CourseTeacher> courseTeacherMap = contentServiceClient.getCourseTeacherBatch(new ArrayList<>(courseIds));

        List<MyCourseTableItemDto> dtoRecords = buildDtoRecords(pageResult.getRecords(), coursePublishMap, courseScoreMap, courseTeacherMap);
        long total = pageResult.getTotal();
        return new PageResult<>(dtoRecords, total, params.getPage(), params.getSize());
    }


}
