package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * @author Ran Lewis
 */
public interface ShoppingCartService {

    /**
     * 添加菜品或套餐
     *
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
