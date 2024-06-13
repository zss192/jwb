package com.jwb.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.content.model.dto.TeachplanDto;
import com.jwb.content.model.po.Teachplan;

import java.util.List;

public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> selectTreeNodes(Long courseId);
}
