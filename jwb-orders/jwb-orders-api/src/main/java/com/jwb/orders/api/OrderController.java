package com.jwb.orders.api;

import com.jwb.base.exception.JwbException;
import com.jwb.orders.model.dto.AddOrderDto;
import com.jwb.orders.model.dto.PayRecordDto;
import com.jwb.orders.service.OrderService;
import com.jwb.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "订单支付接口", tags = "订单支付接口")
@RestController
@Slf4j
public class OrderController {
    @Autowired
    OrderService orderService;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            JwbException.cast("请登录后继续选课");
        }
        return orderService.createOrder(user.getId(), addOrderDto);
    }

}
