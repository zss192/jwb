package com.jwb.orders.service;

import com.jwb.orders.model.dto.OrdersListDto;

import java.util.List;

public interface MyOrdersService {

    /**
     * 获取订单列表
     *
     * @param userId 用户id
     * @return 订单列表
     */
    List<OrdersListDto> getOrdersList(Long userId);

    /**
     * 取消订单
     *
     * @param orderId 订单id
     * @return 是否取消成功
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 删除订单
     *
     * @param orderId 订单id
     * @return 是否删除成功
     */
    Boolean deleteOrder(Long orderId);
}
