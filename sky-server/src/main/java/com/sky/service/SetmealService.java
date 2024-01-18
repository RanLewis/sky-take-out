package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;

import java.util.List;

public interface SetmealService {
    /**
     * 新增套餐
     * @param setmealDTO
     */
    void addSetmeal(SetmealDTO setmealDTO);

}
