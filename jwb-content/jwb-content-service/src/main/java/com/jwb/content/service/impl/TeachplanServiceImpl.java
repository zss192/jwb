package com.jwb.content.service.impl;

import com.jwb.content.mapper.TeachplanMapper;
import com.jwb.content.model.dto.TeachplanDto;
import com.jwb.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
}