package com.jwb.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.jwb.base.exception.JwbException;
import com.jwb.config.MultipartSupportConfig;
import com.jwb.content.feignclient.MediaServiceClient;
import com.jwb.content.feignclient.SearchServiceClient;
import com.jwb.content.mapper.CourseBaseMapper;
import com.jwb.content.mapper.CourseMarketMapper;
import com.jwb.content.mapper.CoursePublishMapper;
import com.jwb.content.mapper.CoursePublishPreMapper;
import com.jwb.content.model.dto.CourseBaseInfoDto;
import com.jwb.content.model.dto.CoursePreviewDto;
import com.jwb.content.model.dto.TeachplanDto;
import com.jwb.content.model.po.CourseBase;
import com.jwb.content.model.po.CourseMarket;
import com.jwb.content.model.po.CoursePublish;
import com.jwb.content.model.po.CoursePublishPre;
import com.jwb.content.service.CourseBaseService;
import com.jwb.content.service.CoursePublishService;
import com.jwb.content.service.TeachplanService;
import com.jwb.messagesdk.model.po.MqMessage;
import com.jwb.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private CourseBaseService courseBaseService;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    // 注入消息SDK
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //查询课程发布信息 使用redis优化
        CoursePublish coursePublish = getCoursePublishCache(courseId);
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 根据课程id，查询课程计划
        List<TeachplanDto> teachplanDtos = teachplanService.findTeachplanTree(courseId);
        // 封装返回
        coursePreviewDto.setCourseBase(courseBase);
        coursePreviewDto.setTeachplans(teachplanDtos);
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 查询课程基本信息、课程营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        // 查询课程计划
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        // 1. 约束
        String auditStatus = courseBaseInfo.getAuditStatus();
        // 1.1 审核完后，方可提交审核
        if ("202003".equals(auditStatus)) {
            JwbException.cast("该课程现在属于待审核状态，审核完成后可再次提交");
        }
        // 1.2 本机构只允许提交本机构的课程
        if (!companyId.equals(courseBaseInfo.getCompanyId())) {
            JwbException.cast("本机构只允许提交本机构的课程");
        }
        // 1.3 没有上传图片，不允许提交
        if (StringUtils.isEmpty(courseBaseInfo.getPic())) {
            JwbException.cast("没有上传课程封面，不允许提交审核");
        }
        // 1.4 没有添加课程计划，不允许提交审核
        if (teachplanTree.isEmpty()) {
            JwbException.cast("没有添加课程计划，不允许提交审核");
        }
        // 2. 准备封装返回对象
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));
        coursePublishPre.setTeachplan(JSON.toJSONString(teachplanTree));
        coursePublishPre.setCompanyId(companyId);
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 3. 设置预发布记录状态为审核通过（暂时跳过审核直接审核通过）
        coursePublishPre.setStatus("202004");
        // 判断是否已经存在预发布记录，若存在，则更新
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        // 4. 设置课程基本信息审核状态为审核通过（暂时跳过审核直接审核通过）
        courseBase.setAuditStatus("202004");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publishCourse(Long companyId, Long courseId) {
        // 1. 约束校验
        // 1.1 获取课程预发布表数据
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            JwbException.cast("请先提交课程审核，审核通过后方可发布");
        }
        // 1.2 课程审核通过后，方可发布
        if (!"202004".equals(coursePublishPre.getStatus())) {
            JwbException.cast("操作失败，课程审核通过后方可发布");
        }
        // 1.3 本机构只允许发布本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            JwbException.cast("操作失败，本机构只允许发布本机构的课程");
        }
        // 2. 向课程发布表插入数据
        saveCoursePublish(courseId);
        // 3. 向消息表插入数据
        saveCoursePublishMessage(courseId);
        // 4. 删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * 保存课程发布信息
     *
     * @param courseId 课程id
     */
    private void saveCoursePublish(Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            JwbException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        // 设置发布状态为已发布
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        // 有则更新，无则新增
        if (coursePublishUpdate == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        // 更新课程基本信息表的发布状态为已发布
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("203002");
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

        // 信息同步更新到Redis 先更新后删除
        String cacheKey = "course:" + courseId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 保存消息表
     *
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            JwbException.cast("添加消息记录失败");
        }
    }

    /**
     * 生成静态页面文件
     *
     * @param courseId 课程id
     */
    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
            // 1. 创建一个Freemarker配置
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 2. 告诉Freemarker在哪里可以找到模板文件
            String classPath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
            configuration.setDefaultEncoding("utf-8");
            // 3. 创建一个模型数据，与模板文件中的数据模型保持一致，这里是CoursePreviewDto类型
            CoursePreviewDto coursePreviewDto = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewDto);
            // 4. 加载模板文件
            Template template = configuration.getTemplate("course_template.ftl");
            // 5. 将数据模型应用于模板
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 5.1 将静态文件内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            htmlFile = File.createTempFile("course", ".html");
            FileOutputStream fos = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, fos);
        } catch (Exception e) {
            log.debug("课程静态化失败：{}", e.getMessage());
            e.printStackTrace();
        }
        return htmlFile;
    }

    /**
     * 将静态页面文件上传到MinIO
     *
     * @param courseId 课程id
     * @param file     静态化文件
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course", courseId + ".html", "静态页面");
        if (course == null) {
            JwbException.cast("远程调用媒资服务上传文件失败");
        }
    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {
        // 发送消息到MQ，添加或修改课程索引
        rabbitTemplate.convertAndSend("course.topic.exchange", "course.insert", courseId);
        return true;
    }

    /**
     * 直接从数据库查询
     *
     * @param courseId
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    /**
     * 从redis + redission查询，适用于分布式
     *
     * @param courseId
     */
    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        String cacheKey = "course:" + courseId;

        // 从缓存中查询
        String courseCacheJson = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotEmpty(courseCacheJson)) {
            log.debug("从缓存中查询");
            return "null".equals(courseCacheJson) ? null : JSON.parseObject(courseCacheJson, CoursePublish.class);
        }

        // 加分布式锁防止缓存击穿和缓存雪崩
        RLock lock = redissonClient.getLock("courseQueryLock" + courseId);
        lock.lock();
        try {
            // 再次从缓存中查询，避免并发时重复查询数据库
            courseCacheJson = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.isNotEmpty(courseCacheJson)) {
                log.debug("从缓存中查询");
                return "null".equals(courseCacheJson) ? null : JSON.parseObject(courseCacheJson, CoursePublish.class);
            }

            log.debug("缓存中没有，查询数据库");
            CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
            if (coursePublish == null) {
                // 缓存空值防止缓存穿透
                redisTemplate.opsForValue().set(cacheKey, "null", 30 + new Random().nextInt(100), TimeUnit.SECONDS);
                return null;
            }

            // 缓存查询结果
            String jsonString = JSON.toJSONString(coursePublish);
            // 过期时间加上一个随机值防止缓存雪崩
            redisTemplate.opsForValue().set(cacheKey, jsonString, 300 + new Random().nextInt(100), TimeUnit.SECONDS);
            return coursePublish;
        } finally {
            lock.unlock();
        }
    }

}
