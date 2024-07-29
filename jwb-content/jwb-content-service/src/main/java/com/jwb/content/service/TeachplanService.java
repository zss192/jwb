package com.jwb.content.service;

import com.jwb.content.model.dto.BindTeachplanMediaDto;
import com.jwb.content.model.dto.SaveTeachplanDto;
import com.jwb.content.model.dto.TeachplanDto;
import com.jwb.content.model.po.Teachplan;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(Long courseId);

    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachplan(Long teachplanId);

    void orderByTeachplan(String moveType, Long teachplanId);

    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    void unassociationMedia(Long teachPlanId, String mediaId);

    Teachplan getTeachplan(Long teachplanId);

    Boolean ifExistMedia(String mediaId);
}
