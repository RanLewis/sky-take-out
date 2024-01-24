package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Ran Lewis
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单，订单信息为：{}", ordersSubmitDTO);
        OrderSubmitVO order = orderService.orderSubmit(ordersSubmitDTO);
        return Result.success(order);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success();
    }

    /**
     * 获取历史订单
     *
     * @param
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("获取历史订单")
    public Result<PageResult> historyOrders(int page, int pageSize, Integer status) {
        log.info("获取历史订单，查询条件为：{}", page, pageSize, status);
        PageResult pageResult = orderService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 获取订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("获取订单详情")
    public Result<OrderVO> getDetailOrder(@PathVariable Long id) {
        log.info("获取订单详情，订单id为：{}", id);
        OrderVO order = orderService.getOrderById(id);
        return Result.success(order);
    }
}
