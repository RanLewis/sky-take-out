package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from sky_take_out.setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增套餐
     *
     * @param setmealVO
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into sky_take_out.setmeal (category_id, status, description, image, name, price," +
            " create_time, create_user, update_time, update_user) " +
            "values (#{categoryId}, #{status}, #{description}, #{image}, #{name}, " +
            "#{price}, #{createeTime}, #{createUser}, #{updateTime}, #{updateUser})")
    void insertSetmeal(SetmealVO setmealVO);
}
