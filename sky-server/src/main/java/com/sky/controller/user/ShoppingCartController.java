package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Ran Lewis
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api("C端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品或套餐
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加菜品或套餐")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加菜品或套餐：{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> list = shoppingCartService.list();
        return Result.success(list);
    }

    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车中一个商品：{}", shoppingCartDTO);
        shoppingCartService.deleteOne(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result cleanShoppingCart() {
        log.info("清空购物车");
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }
}
