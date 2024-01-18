package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     */
    @PostMapping
    @ApiOperation(value = "新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.addDishWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishDTO) {
        log.info("菜品分页查询:{}", dishDTO);
        return Result.success(dishService.pageQuery(dishDTO));
    }

    /**
     * 批量删除菜品
     */
    @DeleteMapping
    @ApiOperation(value = "删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids) {
        log.info("删除菜品:{}", ids);
        dishService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     */
    @GetMapping("{id}")
    @ApiOperation(value = "根据id查询菜品")
    public Result<DishVO> selectDishById(@PathVariable Long id) {
        log.info("根据id查询菜品:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation(value = "修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品:{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
}