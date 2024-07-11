package com.jwb.orders.service;

import com.jwb.orders.model.dto.AddOrderDto;
import com.jwb.orders.model.dto.PayRecordDto;

public interface OrderService {

    /**
     * 创建商品订单
     *
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return 支付交易记录
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

}
