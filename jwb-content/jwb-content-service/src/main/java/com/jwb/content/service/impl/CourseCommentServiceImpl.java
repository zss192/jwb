package com.jwb.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.content.mapper.CourseBaseMapper;
import com.jwb.content.mapper.CourseCommentMapper;
import com.jwb.content.mapper.CourseScoreMapper;
import com.jwb.content.model.dto.QueryCommentDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseComment;
import com.jwb.content.model.po.CourseScore;
import com.jwb.content.service.CourseCommentService;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zss
 * @description 针对表【course_comment】的数据库操作Service实现
 * @createDate 2024-08-03 15:59:20
 */
@Service
public class CourseCommentServiceImpl extends ServiceImpl<CourseCommentMapper, CourseComment>
        implements CourseCommentService {

    @Autowired
    private CourseCommentMapper courseCommentMapper;
    @Autowired
    private CourseBaseMapper CourseBaseMapper;
    @Autowired
    private CourseScoreMapper courseScoreMapper;
    @Autowired
    private CourseCommentService courseCommentService;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    /**
     * 添加评论
     *
     * @param courseComment 评论信息
     * @return 评论信息
     */
    @Override
    @Transactional
    public CourseComment addComment(CourseComment courseComment) {
        // 根据用户id和课程id查询是否已经评论过
        LambdaQueryWrapper<CourseComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseComment::getCourseId, courseComment.getCourseId());
        queryWrapper.eq(CourseComment::getUserId, courseComment.getUserId());
        CourseComment comment = courseCommentMapper.selectOne(queryWrapper);
        if (comment != null) {
            JwbException.cast("您已经评论过该课程");
        }
        CourseBase courseBase = CourseBaseMapper.selectById(courseComment.getCourseId());
        String CourseName = courseBase.getName();
        courseComment.setCourseName(CourseName);
        courseComment.setCreateTime(LocalDateTime.now());
        courseCommentMapper.insert(courseComment);
        // 更新课程评分
        updateCourseScore(courseComment.getCourseId());
        return courseComment;
    }

    @Transactional
    @Override
    public void updateCourseScore(Long courseId) {
        // 查询课程评分
        LambdaQueryWrapper<CourseComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseComment::getCourseId, courseId);
        List<CourseComment> courseComments = courseCommentMapper.selectList(queryWrapper);

        // 计算课程平均分、评分人数、评分总分
        double avgScore = courseComments.stream().mapToDouble(CourseComment::getStarRank).average().orElse(0);
        long peopleCount = courseComments.size();
        double totalScore = courseComments.stream().mapToDouble(CourseComment::getStarRank).sum();

        // 计算五星/四星/三星/二星/一星评分，四舍五入计算星级
        Map<Long, Long> starCounts = courseComments.stream()
                .collect(Collectors.groupingBy(comment -> Math.round(comment.getStarRank()), Collectors.counting()));

        long oneStar = starCounts.getOrDefault(1L, 0L);
        long twoStar = starCounts.getOrDefault(2L, 0L);
        long threeStar = starCounts.getOrDefault(3L, 0L);
        long fourStar = starCounts.getOrDefault(4L, 0L);
        long fiveStar = starCounts.getOrDefault(5L, 0L);

        // 更新课程评分，无则新增，有则更新
        CourseScore courseScore = courseScoreMapper.selectOne(
                new LambdaQueryWrapper<CourseScore>().eq(CourseScore::getCourseId, courseId)
        );

        if (courseScore == null) {
            courseScore = new CourseScore();
            courseScore.setCourseId(courseId);
            courseScoreMapper.insert(courseScore);
        }

        courseScore.setAvgScore(avgScore);
        courseScore.setSumScore(totalScore);
        courseScore.setPeopleCount(peopleCount);
        courseScore.setOneScore(oneStar);
        courseScore.setTwoScore(twoStar);
        courseScore.setThreeScore(threeStar);
        courseScore.setFourScore(fourStar);
        courseScore.setFiveScore(fiveStar);

        courseScoreMapper.updateById(courseScore);

        // 信息同步更新到Redis 先更新后删除
        String cacheKey = "course_score:" + courseId;
        redisTemplate.delete(cacheKey);
    }


    /**
     * 获取指定课程评论
     *
     * @param pageParams      分页参数
     * @param queryCommentDto 查询条件
     * @return 评论列表
     */
    @Override
    public PageResult<CourseComment> getComment(PageParams pageParams, QueryCommentDto queryCommentDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(queryCommentDto.getCourseId()), CourseComment::getCourseId, queryCommentDto.getCourseId());
        queryWrapper.like(StringUtils.isNotEmpty(queryCommentDto.getCourseName()), CourseComment::getCourseName, queryCommentDto.getCourseName());
        // 评分等级 -1差评(0-1) 0中评(2-4) 1好评(4-5)
        if (queryCommentDto.getLevel() != null) {
            if (queryCommentDto.getLevel() == -1) {
                queryWrapper.lt(CourseComment::getStarRank, 2);
            } else if (queryCommentDto.getLevel() == 0) {
                queryWrapper.between(CourseComment::getStarRank, 2, 4);
            } else if (queryCommentDto.getLevel() == 1) {
                queryWrapper.ge(CourseComment::getStarRank, 4);
            }
        }
        queryWrapper.orderByDesc(CourseComment::getCreateTime);
        //分页对象
        Page<CourseComment> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseComment> pageResult = courseCommentMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseComment> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteComment(Long id) {
        Long courseId = courseCommentMapper.selectById(id).getCourseId();
        int res = courseCommentMapper.deleteById(id);
        // 更新课程评分
        courseCommentService.updateCourseScore(courseId);
        return res > 0;
    }

    /**
     * 获取课程评分
     *
     * @param courseId 课程id
     * @return 课程评分
     */
    @Override
    public CourseScore getCourseScore(Long courseId) {
        String cacheKey = "course_score:" + courseId;
        // 从缓存中查询
        String courseScoreCacheJson = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotEmpty(courseScoreCacheJson)) {
            log.debug("从缓存中查询课程评分");
            return "null".equals(courseScoreCacheJson) ? null : JSON.parseObject(courseScoreCacheJson, CourseScore.class);
        }

        // 加分布式锁防止缓存击穿和缓存雪崩
        RLock lock = redissonClient.getLock("courseScoreQueryLock" + courseId);
        lock.lock();
        try {
            // 再次从缓存中查询，避免并发时重复查询数据库
            courseScoreCacheJson = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotEmpty(courseScoreCacheJson)) {
                log.debug("从缓存中查询课程评分");
                return "null".equals(courseScoreCacheJson) ? null : JSON.parseObject(courseScoreCacheJson, CourseScore.class);
            }

            log.debug("缓存中没有课程评分，查询数据库");
            CourseScore courseScore = courseScoreMapper.selectOne(new LambdaQueryWrapper<CourseScore>().eq(CourseScore::getCourseId, courseId));
            if (courseScore == null) {
                // 缓存空值防止缓存穿透
                redisTemplate.opsForValue().set(cacheKey, "null", 5 + new Random().nextInt(10), TimeUnit.SECONDS);
                return null;
            }
            // 缓存查询结果
            String jsonString = JSON.toJSONString(courseScore);
            // 过期时间加上一个随机值防止缓存雪崩
            redisTemplate.opsForValue().set(cacheKey, jsonString, 900 + new Random().nextInt(100), TimeUnit.SECONDS);
            return courseScore;
        } finally {
            lock.unlock();
        }
    }
}




