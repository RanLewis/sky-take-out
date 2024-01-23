package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Ran Lewis
 */
@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车列表
     *
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id更新购物车数量
     *
     * @param id
     * @param number
     */
    @Update("update sky_take_out.shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(Long id, Integer number);

    /**
     * 新增购物车
     *
     * @param shoppingCart
     */
    @Insert("insert into sky_take_out.shopping_cart " +
            "(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "VALUES " +
            "(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insertShoppingCart(ShoppingCart shoppingCart);

    /**
     * 删除购物车
     *
     * @param shoppingCart
     */
    void delete(ShoppingCart shoppingCart);
}
