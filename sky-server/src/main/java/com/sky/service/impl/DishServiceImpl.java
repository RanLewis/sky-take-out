package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品与口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void addDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
        if (flavors != null && !flavors.isEmpty()) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询
     * @param dishDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishDTO) {
        PageHelper.startPage(dishDTO.getPage(), dishDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除
     * @param ids
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        // 起售中菜品不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 被套餐关联的菜品不能删除
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (!setmealIdsByDishIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除关联口味与菜品
        for (Long id : ids) {
            dishFlavorMapper.deleteByDishId(id);
            dishMapper.deleteById(id);
        }

    }

}
