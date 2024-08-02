package com.jwb.orders.model.dto;

import lombok.Data;

@Data
public class QueryOrdersDto {
    String courseName;
    String orderStart;
    String orderEnd;
    String status;
    String orderNo;
    String userId;
}
