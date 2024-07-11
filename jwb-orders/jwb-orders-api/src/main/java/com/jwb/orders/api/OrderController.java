package com.jwb.orders.api;

import com.alipay.api.AlipayApiException;
import com.jwb.base.exception.JwbException;
import com.jwb.orders.model.dto.AddOrderDto;
import com.jwb.orders.model.dto.PayRecordDto;
import com.jwb.orders.service.OrderService;
import com.jwb.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "订单支付接口", tags = "订单支付接口")
@RestController
@Slf4j
public class OrderController {
    @Autowired
    OrderService orderService;

    // TODO：优化流程，前端目前刚开始展示的是没法显示的图片
    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            JwbException.cast("请登录后继续选课");
        }
        return orderService.createOrder(user.getId(), addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse response) throws IOException, AlipayApiException {
        orderService.scanToPay(payNo, response);
    }

    // 前端点击 支付完成 主动查询，然后更新订单表（前端按钮已隐藏）
    @ApiOperation("主动查询支付结果")
    @GetMapping("/payresult")
    public PayRecordDto payresult(String payNo) {
        return orderService.queryPayResult(payNo);
    }

    // 扫码支付后通过回调地址自动请求该接口
    // TODO：支付成功后前端要手动刷新，优化流程
    @ApiOperation("被动支付通知")
    @PostMapping("/paynotify")
    public void paynotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        orderService.payNotify(request, response);
    }
}
