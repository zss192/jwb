package com.jwb.orders.service;

import com.alipay.api.AlipayApiException;
import com.jwb.messagesdk.model.po.MqMessage;
import com.jwb.orders.model.dto.AddOrderDto;
import com.jwb.orders.model.dto.PayRecordDto;
import com.jwb.orders.model.dto.PayStatusDto;
import com.jwb.orders.model.po.JwbPayRecord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface OrderService {

    /**
     * 创建商品订单
     *
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return 支付交易记录
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付交易记录
     *
     * @param payNo 交易记录号
     */
    JwbPayRecord getPayRecordByPayNo(String payNo);

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);

    void saveAlipayStatus(PayStatusDto payStatusDto);

    void scanToPay(String payNo, HttpServletResponse response) throws AlipayApiException, IOException;

    void payNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, IOException;

    /**
     * 发送通知结果
     *
     * @param mqMessage 消息
     */
    void notifyPayResult(MqMessage mqMessage);
}
