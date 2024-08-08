package com.jwb.orders.task;

import com.jwb.orders.config.MqDelayConfig;
import com.jwb.orders.mapper.JwbOrdersMapper;
import com.jwb.orders.model.po.JwbOrders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    JwbOrdersMapper ordersMapper;

    @RabbitListener(queues = MqDelayConfig.QUEUE_NAME)
    public void processTimeoutOrder(Long orderId) {
        // 通过从队列中获取的订单号，查询订单
        JwbOrders order = ordersMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在，订单号：" + orderId);
            return;
        }
        // 如果订单状态为未支付，则取消订单
        if ("600001".equals(order.getStatus())) {
            order.setStatus("600003"); // 订单状态为已取消
            ordersMapper.updateById(order);
            log.debug("订单超时，取消订单，订单号：" + orderId + "，时间：" + LocalDateTime.now());
        }
    }
}
