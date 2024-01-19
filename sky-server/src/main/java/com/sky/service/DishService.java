package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;


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

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 根据id修改菜品状态
     * @param status
     * @param id
     */
    void updateStatus(Integer status, Long id);
}
