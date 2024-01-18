package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     *
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 新增套餐-菜品关联表
     *
     * @param setmealDish
     */
    @Insert("insert into sky_take_out.setmeal_dish (setmeal_id, dish_id, copies, price, name) " +
            "values (#{setmealId}, #{dishId}, #{copies}, #{price}, #{name})")
    void insert(SetmealDish setmealDish);
}
