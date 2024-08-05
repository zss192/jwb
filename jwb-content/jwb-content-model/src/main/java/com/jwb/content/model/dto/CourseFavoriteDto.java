package com.jwb.content.model.dto;

import com.jwb.content.model.po.CourseBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CourseFavoriteDto extends CourseBase {
    Double avgScore;
}
