package com.jwb.orders.service;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.orders.model.dto.OrdersListDto;
import com.jwb.orders.model.dto.QueryOrdersDto;
import com.jwb.orders.model.po.JwbOrders;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * 机构端查询所有订单列表
     *
     * @return 订单列表
     */
    PageResult<JwbOrders> getAllOrdersList(PageParams pageParams, @RequestBody QueryOrdersDto queryOrdersDto);
}
