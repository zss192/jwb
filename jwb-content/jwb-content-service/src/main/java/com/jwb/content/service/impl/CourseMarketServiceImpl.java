package com.jwb.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jwb.content.mapper.CourseMarketMapper;
import com.jwb.content.model.po.CourseMarket;
import com.jwb.content.service.CourseMarketService;
import org.springframework.stereotype.Service;

@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket> implements CourseMarketService {
}