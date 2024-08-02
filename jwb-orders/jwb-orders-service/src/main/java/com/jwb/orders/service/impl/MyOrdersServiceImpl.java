package com.jwb.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jwb.base.exception.JwbException;
import com.jwb.base.model.PageParams;
import com.jwb.base.model.PageResult;
import com.jwb.orders.feignclient.ContentServiceClient;
import com.jwb.orders.mapper.JwbOrdersGoodsMapper;
import com.jwb.orders.mapper.JwbOrdersMapper;
import com.jwb.orders.model.dto.OrdersListDto;
import com.jwb.orders.model.dto.QueryOrdersDto;
import com.jwb.orders.model.po.CourseBase;
import com.jwb.orders.model.po.JwbOrders;
import com.jwb.orders.model.po.JwbOrdersGoods;
import com.jwb.orders.service.MyOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MyOrdersServiceImpl implements MyOrdersService {

    @Autowired
    JwbOrdersMapper OrdersMapper;

    @Autowired
    JwbOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    ContentServiceClient contentServiceClient;


    /**
     * 获取订单列表
     *
     * @param userId 用户id
     * @return 订单列表
     */
    @Override
    public List<OrdersListDto> getOrdersList(Long userId) {
        List<OrdersListDto> ordersList = new ArrayList<>();
        // 按照时间倒序查询订单
        List<JwbOrders> orders = OrdersMapper.selectList(new LambdaQueryWrapper<JwbOrders>()
                .eq(JwbOrders::getUserId, userId)
                .orderByDesc(JwbOrders::getCreateDate));

        orders.forEach(order -> {
            // 取出order_detail中的goodsId去查课程基本信息
            // [{"goodsId":"1818933396148207618","goodsType":"60201","goodsName":"前端Web开发HTML5+CSS3视频教程","goodsPrice":35}]
            String orderDetail = order.getOrderDetail();
            // 解析json,，取出goodsId
            List<JwbOrdersGoods> ordersGoodsList = JSON.parseArray(orderDetail, JwbOrdersGoods.class);
            String courseId = ordersGoodsList.get(0).getGoodsId();
            CourseBase courseBase = contentServiceClient.getCourseBaseById(Long.valueOf(courseId));
            OrdersListDto ordersListDto = new OrdersListDto();
            ordersListDto.setOrders(order);
            ordersListDto.setCourseBase(courseBase);
            ordersList.add(ordersListDto);
        });
        return ordersList;
    }

    /**
     * 取消订单
     *
     * @param orderId 订单id
     * @return 是否取消成功
     */
    @Override
    public Boolean cancelOrder(Long orderId) {
        JwbOrders order = OrdersMapper.selectById(orderId);
        if (order == null) {
            JwbException.cast("订单不存在");
        }
        if ("600002".equals(order.getStatus())) {
            JwbException.cast("订单已支付，无法取消");
        }
        order.setStatus("600003");
        int update = OrdersMapper.updateById(order);
        return update > 0;
    }

    /**
     * 删除订单
     *
     * @param orderId 订单id
     * @return 是否删除成功
     */
    @Transactional
    @Override
    public Boolean deleteOrder(Long orderId) {
        int delete1 = OrdersMapper.deleteById(orderId);
        int delete2 = ordersGoodsMapper.delete(new LambdaQueryWrapper<JwbOrdersGoods>().eq(JwbOrdersGoods::getOrderId, orderId));
        return delete1 > 0 && delete2 > 0;
    }

    /**
     * 机构端查询所有订单列表
     *
     * @return 订单列表
     */
    @Override
    public PageResult<JwbOrders> getAllOrdersList(PageParams pageParams, @RequestBody QueryOrdersDto queryOrdersDto) {
        //构建查询条件对象
        LambdaQueryWrapper<JwbOrders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryOrdersDto.getCourseName()), JwbOrders::getOrderName, queryOrdersDto.getCourseName());
        queryWrapper.like(StringUtils.isNotEmpty(queryOrdersDto.getOrderNo()), JwbOrders::getId, queryOrdersDto.getOrderNo());
        queryWrapper.eq(StringUtils.isNotEmpty(queryOrdersDto.getStatus()), JwbOrders::getStatus, queryOrdersDto.getStatus());
        queryWrapper.eq(StringUtils.isNotEmpty(queryOrdersDto.getUserId()), JwbOrders::getUserId, queryOrdersDto.getUserId());
        queryWrapper.ge(StringUtils.isNotEmpty(queryOrdersDto.getOrderStart()), JwbOrders::getCreateDate, queryOrdersDto.getOrderStart());
        queryWrapper.le(StringUtils.isNotEmpty(queryOrdersDto.getOrderEnd()), JwbOrders::getCreateDate, queryOrdersDto.getOrderEnd());
        //分页对象
        Page<JwbOrders> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<JwbOrders> pageResult = OrdersMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<JwbOrders> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }
}
