package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.common.CustomException;
import com.ahiru.reggie.entity.Category;
import com.ahiru.reggie.entity.Dish;
import com.ahiru.reggie.entity.Setmeal;
import com.ahiru.reggie.mapper.CategoryMapper;
import com.ahiru.reggie.service.CategoryService;
import com.ahiru.reggie.service.DishService;
import com.ahiru.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    //自定义删除方法，删除前判断分类是否关联了菜品或套餐
    @Override
    public void checkAndRemove(Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int countDish = dishService.count(dishLambdaQueryWrapper);
        if (countDish>0){
            throw new CustomException("当前分类关联了菜品，不能删除");
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int countSetmeal = setmealService.count(setmealLambdaQueryWrapper);
        if(countSetmeal>0){
            throw new CustomException("当前分类关联了套餐，不能删除");
        }
        super.removeById(id);

    }
}
