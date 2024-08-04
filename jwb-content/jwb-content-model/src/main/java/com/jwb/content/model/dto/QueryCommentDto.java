package com.jwb.content.model.dto;

import lombok.Data;

@Data
public class QueryCommentDto {
    String courseId;
    String courseName;
    Integer level; // -1差评 0中评 1好评
}
