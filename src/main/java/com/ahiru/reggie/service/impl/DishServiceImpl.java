package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.dto.DishDto;
import com.ahiru.reggie.entity.Dish;
import com.ahiru.reggie.entity.DishFlavor;
import com.ahiru.reggie.mapper.DishMapper;
import com.ahiru.reggie.service.DishFlavorService;
import com.ahiru.reggie.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    //新增
    @Override
    @Transactional //事务的注解，因为要同时操作两张表
    public void saveWithFlavor(DishDto dishDto) {

        log.info("dishDto："+dishDto);

        //存Dish（生成了dishId
        this.save(dishDto);

        //获取id
        Long dishId = dishDto.getId();
        log.info("存Dish之后的id是"+dishId);

        //存DishFlavor
        List<DishFlavor> flavors = dishDto.getFlavors(); //包含flavor的id、name、value，缺少菜品id
        log.info("setDishId之前的flavors"+flavors);
        flavors = flavors.stream().map((item) -> {  //stream流的map方法可以将每个对象映射为新结果
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());  //collect将流对象转换为集合
        log.info("setDishId之后的flavors"+flavors);

        //存DishFlavor
        dishFlavorService.saveBatch(flavors);

    }

    //回显
    @Override
    public DishDto getWithFlavor(String id) {

        //最终要返回disDto，需要Dish和Dish的Flavor属性
        DishDto dishDto = new DishDto();

        //查到dish
        Dish dish = this.getById(id);

        //先将dish拷贝到dishDto
        BeanUtils.copyProperties(dish, dishDto);

        //再查出dish的Flavor属性
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //查DishFlavor中id匹配的flavor
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //赋给ditDo
        dishDto.setFlavors(list);
        log.info("根据id({})查到dishDto：{}", id, dishDto);

        return dishDto;
    }

    //修改菜品
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish
        this.updateById(dishDto);

        //清空相关的Flavor表信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        boolean remove = dishFlavorService.remove(queryWrapper);
        log.info("清空相关的Flavor表信息"+remove);

        //添加Flavor信息
        List<DishFlavor> flavors = dishDto.getFlavors(); //缺少DishId
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }


}
