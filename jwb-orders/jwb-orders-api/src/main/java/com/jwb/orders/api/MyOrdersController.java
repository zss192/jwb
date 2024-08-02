package com.jwb.orders.api;

import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.orders.model.dto.OrdersListDto;
import com.jwb.orders.model.dto.QueryOrdersDto;
import com.jwb.orders.model.po.JwbOrders;
import com.jwb.orders.service.MyOrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "我的订单接口", tags = "我的订单接口")
@RestController
@Slf4j
public class MyOrdersController {
    @Autowired
    private MyOrdersService myOrdersService;

    @ApiOperation("查询订单列表")
    @GetMapping("/list/{userId}")
    public List<OrdersListDto> getOrdersList(@PathVariable Long userId) {
        return myOrdersService.getOrdersList(userId);
    }

    @ApiOperation("机构端查询所有订单列表")
    @PostMapping("/orders/org-list")
    public PageResult<JwbOrders> getAllOrdersList(PageParams pageParams, @RequestBody QueryOrdersDto queryOrdersDto) {
        return myOrdersService.getAllOrdersList(pageParams, queryOrdersDto);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancelOrder/{orderId}")
    public Boolean cancelOrder(@PathVariable Long orderId) {
        return myOrdersService.cancelOrder(orderId);
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/deleteOrder/{orderId}")
    public Boolean deleteOrder(@PathVariable Long orderId) {
        return myOrdersService.deleteOrder(orderId);
    }
}
