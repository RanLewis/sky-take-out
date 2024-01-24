package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Ran Lewis
 */
@Mapper
public interface OrderDetailMapper {
    void insertList(List<OrderDetail> orderDetailList);

    /**
     * 根据订单号查询订单详情
     * @param orderId
     * @return
     */
    @Select("select * from sky_take_out.order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
