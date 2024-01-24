package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Ran Lewis
 */
@Mapper
public interface OrderDetailMapper {
    void insertList(List<OrderDetail> orderDetailList);
}
