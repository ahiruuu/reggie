package com.ahiru.reggie.service;

import com.ahiru.reggie.dto.DishDto;
import com.ahiru.reggie.entity.Dish;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DishService extends IService<Dish> {

    //自定义一个方法，新增菜品时操作两张表
    void saveWithFlavor(DishDto dishDto);

    //自定义一个方法，修改菜品时回显信息
    DishDto getWithFlavor(String id);

    //自定义一个方法，新增菜品
    void updateWithFlavor(DishDto dishDto);

}
