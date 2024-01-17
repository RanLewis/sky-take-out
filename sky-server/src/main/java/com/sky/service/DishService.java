package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;


import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     */
    void addDishWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询
     * @param dishDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishDTO);

    /**
     * 根据id删除菜品
     * @return
     */
    void deleteByIds(List<Long> ids);
}
