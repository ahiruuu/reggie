package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.dto.DishDto;
import com.ahiru.reggie.dto.SetmealDto;
import com.ahiru.reggie.entity.*;
import com.ahiru.reggie.mapper.SetmealMapper;
import com.ahiru.reggie.service.SetmealDishService;
import com.ahiru.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    //回显
    @Override
    public SetmealDto getWithDish(String id) {

        //最终要返回setmealDto，需要补充List<SetmealDish>属性
        SetmealDto setmealDto = new SetmealDto();

        //查到setmeal
        Setmeal setmeal = this.getById(id);
        //先将setmeal拷贝到setmealDto
        BeanUtils.copyProperties(setmeal, setmealDto);

        //再查出setmeal的List<SetmealDish>属性
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //查SetmealDish中id匹配的setmealDish
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        //赋给ditDo
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    //新增
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        //先存setmeal的信息
        this.save(setmealDto);

        //获取id
        Long setmealId = setmealDto.getId();

        //存SetmealDish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();//缺少套餐id
        setmealDishes = setmealDishes.stream().map((item) -> {  //stream流的map方法可以将每个对象映射为新结果
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    //修改
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal
        this.updateById(setmealDto);

        //清空setmealDish表信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //重新添加setmealDish信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

}
