package com.jwb.content.service;

import com.jwb.content.model.dto.SaveTeachplanDto;
import com.jwb.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(Long courseId);

    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);
}
