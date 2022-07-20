package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.BaseContext;
import com.ahiru.reggie.common.R;
import com.ahiru.reggie.entity.ShoppingCart;
import com.ahiru.reggie.service.ShoppingCartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //添加菜品/套餐到购物车
    @PostMapping("/add")
//    {amount: 299
//    dishFlavor: "不要葱,重辣"
//    dishId: "1548187189865730049"
//    image: "6508b795-eade-4998-aad1-907756a94318.png"
//    name: "爆辣鸭头"}
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加到购物车"+shoppingCart);

        //设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询菜品/套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //限定范围：当前用户
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        //判断要添加的是菜品还是套餐
        if(shoppingCart.getDishId()!=null){
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else{
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //查菜品/套餐是否已存在
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if(one==null){
            //不存在，添加，数量为1
            shoppingCart.setNumber(1);
            //设置添加时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;

        }else {
            //存在，数量+1
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
        }

        return R.success(one);
    }

    //数量-1
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //判断要减一的是菜品还是套餐
        if(shoppingCart.getDishId()!=null){
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else{
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        one.setNumber(one.getNumber()-1);
        if (one.getNumber()==0){
            shoppingCartService.removeById(one);
        }else{
            shoppingCartService.updateById(one);
        }
        return R.success("减1成功");
    }


    //查看购物车
    @GetMapping("/list")
    public R<List<ShoppingCart>> getList(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }


    //清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

}
