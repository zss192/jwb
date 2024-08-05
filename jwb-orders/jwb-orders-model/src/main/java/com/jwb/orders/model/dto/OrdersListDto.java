package com.jwb.orders.model.dto;

import com.jwb.orders.model.po.CourseBase;
import com.jwb.orders.model.po.JwbOrders;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrdersListDto {
    private JwbOrders orders;
    private CourseBase courseBase;
    private Double avgScore; // 评分
}
