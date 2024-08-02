package com.jwb.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jwb.base.exception.JwbException;
import com.jwb.base.utils.IdWorkerUtil;
import com.jwb.base.utils.QRCodeUtil;
import com.jwb.messagesdk.model.po.MqMessage;
import com.jwb.messagesdk.service.MqMessageService;
import com.jwb.orders.config.AlipayConfig;
import com.jwb.orders.config.PayNotifyConfig;
import com.jwb.orders.mapper.JwbOrdersGoodsMapper;
import com.jwb.orders.mapper.JwbOrdersMapper;
import com.jwb.orders.mapper.JwbPayRecordMapper;
import com.jwb.orders.model.dto.AddOrderDto;
import com.jwb.orders.model.dto.PayRecordDto;
import com.jwb.orders.model.dto.PayStatusDto;
import com.jwb.orders.model.po.JwbOrders;
import com.jwb.orders.model.po.JwbOrdersGoods;
import com.jwb.orders.model.po.JwbPayRecord;
import com.jwb.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    JwbOrdersMapper OrdersMapper;

    @Autowired
    JwbPayRecordMapper PayRecordMapper;

    @Autowired
    JwbOrdersGoodsMapper OrdersGoodsMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MqMessageService mqMessageService;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    // 支付成功后回调地址，注意要进行内网穿透
    @Value("${pay.notify-url}")
    String notifyUrl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

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
            String url = qrcodeurl + payRecord.getPayNo();
            // 3.2 生成二维码
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
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
        return OrdersMapper.selectOne(new LambdaQueryWrapper<JwbOrders>()
                .eq(JwbOrders::getOutBusinessId, businessId)
                .eq(JwbOrders::getStatus, "600001") // 待支付
        );
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

    @Override
    public JwbPayRecord getPayRecordByPayNo(String payNo) {
        return PayRecordMapper.selectOne(new LambdaQueryWrapper<JwbPayRecord>().eq(JwbPayRecord::getPayNo, payNo));
    }

    @Override
    public JwbPayRecord queryPayResult(String payNo) {

        // 1. 调用支付宝接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);

        // 2. 拿到支付结果，更新支付记录表和订单表的状态为 已支付
        JwbPayRecord jwbPayRecord = saveAlipayStatus(payStatusDto);

        return jwbPayRecord;
    }

    /**
     * 调用支付宝接口查询支付结果
     *
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {
        // 1. 获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        // 2. 请求查询
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            JwbException.cast("请求支付宝查询支付结果异常");
        }
        // 3. 查询失败
        if (!response.isSuccess()) {
            JwbException.cast("请求支付宝查询支付结果异常");
        }
        // 4. 查询成功，获取结果集
        String resultJson = response.getBody();
        // 4.1 转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        // 4.2 获取我们需要的信息
        Map<String, String> alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        // 5. 创建返回对象
        PayStatusDto payStatusDto = new PayStatusDto();
        // 6. 封装返回
        String tradeStatus = alipay_trade_query_response.get("trade_status");
        String outTradeNo = alipay_trade_query_response.get("out_trade_no");
        String tradeNo = alipay_trade_query_response.get("trade_no");
        String totalAmount = alipay_trade_query_response.get("total_amount");
        payStatusDto.setTrade_status(tradeStatus);
        payStatusDto.setOut_trade_no(outTradeNo);
        payStatusDto.setTrade_no(tradeNo);
        payStatusDto.setTotal_amount(totalAmount);
        payStatusDto.setApp_id(APP_ID);
        return payStatusDto;
    }

    public JwbPayRecord saveAlipayStatus(PayStatusDto payStatusDto) {
        // 1. 获取支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        // 2. 查询数据库订单状态
        JwbPayRecord payRecord = getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            JwbException.cast("未找到支付记录");
        }
        JwbOrders order = OrdersMapper.selectById(payRecord.getOrderId());
        if (order == null) {
            JwbException.cast("找不到相关联的订单");
        }
        String statusFromDB = payRecord.getStatus();
        // 2.1 已支付，直接返回
        if ("600002".equals(statusFromDB)) {
            return null;
        }
        // 3. 查询支付宝交易状态
        String tradeStatus = payStatusDto.getTrade_status();
        // 3.1 支付宝交易已成功，保存订单表和交易记录表，更新交易状态
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 更新支付交易表
            payRecord.setStatus("601002");
            payRecord.setOutPayNo(payStatusDto.getTrade_no());
            payRecord.setOutPayChannel("Alipay");
            payRecord.setPaySuccessTime(LocalDateTime.now());
            int updateRecord = PayRecordMapper.updateById(payRecord);
            if (updateRecord <= 0) {
                JwbException.cast("更新支付交易表失败");
            }
            // 更新订单表
            order.setStatus("600002");
            int updateOrder = OrdersMapper.updateById(order);
            if (updateOrder <= 0) {
                JwbException.cast("更新订单表失败");
            }
        }

        // 4. 保存消息记录，参数1：支付结果类型通知；参数2：业务id；参数3：业务类型
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", order.getOutBusinessId(), order.getOrderType(), null);
        // 5. 发送消息
        notifyPayResult(mqMessage);
        return payRecord;
    }

    @Override
    public void scanToPay(String payNo, HttpServletResponse response) throws AlipayApiException, IOException {
        JwbPayRecord payRecord = getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            JwbException.cast("请重新点击支付获取二维码");
        }
        String status = payRecord.getStatus();
        if ("601002".equals(status)) {
            JwbException.cast("，请勿重复支付");
        }

        // 请求支付宝接口进行支付
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setNotifyUrl(notifyUrl);//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," +
                "    \"total_amount\":" + payRecord.getTotalPrice() + "," +
                "    \"subject\":\"" + payRecord.getOrderName() + "\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");//填充业务参数
        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        response.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        response.getWriter().write(form);//直接将完整的表单html输出到页面
        response.getWriter().flush();
    }

    /**
     * 交易成功后通过回调地址更新订单表和交易记录表
     *
     * @param request
     * @param response
     * @throws AlipayApiException
     * @throws IOException
     */
    @Override
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, IOException {
        Map<String, String> params = getMap(request);
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if (verify_result) {
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            if (trade_status.equals("TRADE_FINISHED")) {//交易结束
                log.debug("交易已结束");
            } else if (trade_status.equals("TRADE_SUCCESS")) {
                // 交易成功，保存订单信息
                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setApp_id(APP_ID);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setTotal_amount(total_amount);
                saveAlipayStatus(payStatusDto);
                log.debug("交易成功");
            }
            response.getWriter().write("success");
        } else {
            response.getWriter().write("fail");
        }
    }

    private Map<String, String> getMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }

    /**
     * 发送通知结果
     *
     * @param mqMessage 消息
     */
    @Override
    public void notifyPayResult(MqMessage mqMessage) {
        // 1. 将消息体转为Json
        String jsonMsg = JSON.toJSONString(mqMessage);
        // 2. 设消息的持久化方式为PERSISTENT，即消息会被持久化到磁盘上，确保即使在RabbitMQ服务器重启后也能够恢复消息。
        Message msgObj = MessageBuilder.withBody(jsonMsg.getBytes()).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
        // 3. 封装CorrelationData，用于跟踪消息的相关信息
        CorrelationData correlationData = new CorrelationData(mqMessage.getId().toString());
        // 3.1 添加一个Callback对象，该对象用于在消息确认时处理消息的结果
        correlationData.getFuture().addCallback(result -> {
            if (result.isAck()) {
                // 3.2 消息发送成功，删除消息表中的记录
                log.debug("消息发送成功：{}", jsonMsg);
                mqMessageService.completed(mqMessage.getId());
            } else {
                // 3.3 消息发送失败
                log.error("消息发送失败，id：{}，原因：{}", mqMessage.getId(), result.getReason());
            }
        }, ex -> {
            // 3.4 消息异常
            log.error("消息发送异常，id：{}，原因：{}", mqMessage.getId(), ex.getMessage());
        });
        // 4. 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }
}
