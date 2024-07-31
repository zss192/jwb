package com.jwb.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CourseDynamicDto", description = "课程动态信息")
public class CourseDynamicDto {
    @ApiModelProperty(value = "学习人数")
    private Long studyCount;

    @ApiModelProperty(value = "收藏人数")
    private Long favoriteCount;
}
