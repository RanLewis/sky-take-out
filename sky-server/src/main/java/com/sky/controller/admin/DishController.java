package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
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
}
