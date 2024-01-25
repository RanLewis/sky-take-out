package com.sky.service.impl;

import com.alibaba.druid.sql.dialect.blink.parser.BlinkStatementParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.JsonObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ran Lewis
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private com.sky.utils.WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {
        // 判断地址簿是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表,订单明细表插入数据,成功清空用户购物车
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);

        // 待付款
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber((System.currentTimeMillis()) + "::" + userId);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertList(orderDetailList);

        shoppingCartMapper.deleteByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }


    /**
     * 获取历史订单
     *
     * @param
     * @return
     */
    @Override
    public PageResult historyOrders(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = OrdersPageQueryDTO.builder()
                .userId(BaseContext.getCurrentId())
                .status(status)
                .build();
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        if (orders.getTotal() > 0 && orders != null) {
            for (Orders order : orders) {
                OrderVO orderVO = new OrderVO();
                Long orderId = order.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(orders.getTotal(), list);
    }

    /**
     * 根据订单id查询订单
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderById(Long id) {
        OrderVO orderVO = new OrderVO();
        Orders order = orderMapper.getById(id);
        BeanUtils.copyProperties(order, orderVO);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 取消订单
     *
     * @param id
     */
    @Override
    public void cancel(Long id) {
        // 查询订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查询账单是否可退款
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 退款

        // 修改订单状态为已取消
        orders.setStatus(Orders.CANCELLED);
        // 取消时间,原因
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orderMapper.update(orders);
    }

    @Override
    public void payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders order = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setPayStatus(Orders.PAID);
        order.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(order);

        // 通过WebSocket发送来单提醒消息
        Map map = new HashMap();
        map.put("typr", 1);
        map.put("orderId", order.getId());
        map.put("content", "订单号: " + ordersPaymentDTO.getOrderNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repetition(Long id) {
        ShoppingCart Cart = new ShoppingCart();
        List<ShoppingCart> list = shoppingCartMapper.list(Cart);
        if (!list.isEmpty()) {
            throw new OrderBusinessException("购物车中有数据");
        }

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        Long userId = BaseContext.getCurrentId();
        for (OrderDetail order : orderDetails) {
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .userId(userId)
                    .build();
            BeanUtils.copyProperties(order, shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insertShoppingCart(shoppingCart);
        }

        Orders orders = orderMapper.getById(id);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);

        // 待付款
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber((System.currentTimeMillis()) + "::" + userId);
        orderMapper.insert(orders);

        List<OrderDetail> orderDetails1 = orderDetailMapper.getByOrderId(id);
        Long orderId = orders.getId();
        for (OrderDetail detail : orderDetails1) {
            detail.setOrderId(orderId);
        }
        orderDetailMapper.insertList(orderDetails1);
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<OrderVO> list = new ArrayList<>();
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);
        if (orders.getTotal() > 0 && orders != null) {
            for (Orders order : orders) {
                OrderVO orderVO = new OrderVO();
                Long orderId = order.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(orders.getTotal(), list);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * @param id
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders1 = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders1 == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders1.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .estimatedDeliveryTime(null)
                .build();
        // 退款
        if (orders1.getPayStatus() == Orders.PAID) {
            // 退款
        }
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders1 = orderMapper.getById(ordersCancelDTO.getId());
        if (orders1 == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders1.getStatus() > 4) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .estimatedDeliveryTime(null)
                .build();
        // 退款
        if (orders1.getPayStatus() == Orders.PAID) {
            // 退款
        }
        orderMapper.update(orders);
    }

    /**
     * 商家发货
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() != Orders.CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = new Orders();
        orders1.setId(orders.getId());
        // 更新订单状态,状态转为派送中
        orders1.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders1);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号: " + ordersDB.getNumber() + " 请尽快送达");
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
