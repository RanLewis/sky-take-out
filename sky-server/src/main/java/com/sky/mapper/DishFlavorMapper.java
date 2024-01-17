package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 根据菜品id删除菜品 flavor
     */
    @Delete("delete from sky_take_out.dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

    /**
     * 批量插入口味
     *
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);
}
