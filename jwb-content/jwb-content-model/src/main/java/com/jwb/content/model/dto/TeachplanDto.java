package com.jwb.content.model.dto;

import com.jwb.content.model.po.Teachplan;
import com.jwb.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachplanDto extends Teachplan {
    private TeachplanMedia teachplanMedia;
    private List<TeachplanDto> teachPlanTreeNodes;
}
