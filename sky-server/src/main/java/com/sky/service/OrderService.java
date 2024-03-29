package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ran Lewis
 */
public interface OrderService {
    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 获取历史订单
     *
     * @param
     * @return
     */
    PageResult historyOrders(int page, int pageSize, Integer status);

    /**
     * 获取订单详情
     *
     * @param id
     * @return
     */
    OrderVO getOrderById(Long id);

    /**
     * 取消订单
     *
     * @param id
     */
    void cancel(Long id);

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    void payment(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 再来一单
     *
     * @param id
     */
    void repetition(Long id);

    /**
     * 条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单数量统计
     *
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     *
     * @param
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    void cancelByAdmin(OrdersCancelDTO ordersCancelDTO);

    /**
     * 订单发货
     *
     * @param id
     */
    void delivery(Long id);

    /**
     * 订单完成
     *
     * @param id
     */
    void complete(Long id);

    /**
     * 催单
     *
     * @param id
     */
    void reminder(Long id);
}
