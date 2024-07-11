package com.jwb.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.base.exception.JwbException;
import com.jwb.base.utils.IdWorkerUtil;
import com.jwb.base.utils.QRCodeUtil;
import com.jwb.orders.mapper.JwbOrdersGoodsMapper;
import com.jwb.orders.mapper.JwbOrdersMapper;
import com.jwb.orders.mapper.JwbPayRecordMapper;
import com.jwb.orders.model.dto.AddOrderDto;
import com.jwb.orders.model.dto.PayRecordDto;
import com.jwb.orders.model.po.JwbOrders;
import com.jwb.orders.model.po.JwbOrdersGoods;
import com.jwb.orders.model.po.JwbPayRecord;
import com.jwb.orders.service.OrderService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    JwbOrdersMapper OrdersMapper;

    @Autowired
    JwbPayRecordMapper PayRecordMapper;

    @Autowired
    JwbOrdersGoodsMapper OrdersGoodsMapper;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // 1. 添加商品订单
        JwbOrders orders = saveOrders(userId, addOrderDto);
        // 2. 添加支付交易记录
        JwbPayRecord payRecord = createPayRecord(orders);
        // 3. 生成二维码
        String qrCode = null;
        try {
            // 3.1 用订单号填充占位符
            qrcodeurl = String.format(qrcodeurl, payRecord.getPayNo());
            // 3.2 生成二维码
            qrCode = new QRCodeUtil().createQRCode(qrcodeurl, 200, 200);
        } catch (IOException e) {
            JwbException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    /**
     * 保存订单信息，保存订单表和订单明细表，需要做幂等性判断
     *
     * @param userId      用户id
     * @param addOrderDto 选课信息
     */
    public JwbOrders saveOrders(String userId, AddOrderDto addOrderDto) {
        // 1. 幂等性判断
        JwbOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        // 2. 插入订单表
        order = new JwbOrders();
        BeanUtils.copyProperties(addOrderDto, order);
        order.setId(IdWorkerUtil.getInstance().nextId());
        order.setCreateDate(LocalDateTime.now());
        order.setUserId(userId);
        order.setStatus("600001");
        int insert = OrdersMapper.insert(order);
        if (insert <= 0) {
            JwbException.cast("插入订单记录失败");
        }
        // 3. 插入订单明细表
        Long orderId = order.getId();
        String orderDetail = addOrderDto.getOrderDetail();
        List<JwbOrdersGoods> OrdersGoodsList = JSON.parseArray(orderDetail, JwbOrdersGoods.class);
        OrdersGoodsList.forEach(goods -> {
            goods.setOrderId(orderId);
            int insert1 = OrdersGoodsMapper.insert(goods);
            if (insert1 <= 0) {
                JwbException.cast("插入订单明细失败");
            }
        });
        return order;
    }

    /**
     * 根据业务id查询订单
     *
     * @param businessId 业务id是选课记录表中的主键
     */
    public JwbOrders getOrderByBusinessId(String businessId) {
        return OrdersMapper.selectOne(new LambdaQueryWrapper<JwbOrders>().eq(JwbOrders::getOutBusinessId, businessId));
    }

    public JwbPayRecord createPayRecord(JwbOrders orders) {
        if (orders == null) {
            JwbException.cast("订单不存在");
        }
        if ("600002".equals(orders.getStatus())) {
            JwbException.cast("订单已支付");
        }
        JwbPayRecord payRecord = new JwbPayRecord();
        payRecord.setPayNo(IdWorkerUtil.getInstance().nextId());
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");  // 未支付
        payRecord.setUserId(orders.getUserId());
        int insert = PayRecordMapper.insert(payRecord);
        if (insert <= 0) {
            JwbException.cast("插入支付交易记录失败");
        }
        return payRecord;
    }


}
