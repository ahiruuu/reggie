package com.ahiru.reggie.service;

import com.ahiru.reggie.dto.SetmealDto;
import com.ahiru.reggie.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SetmealService extends IService<Setmeal> {

    SetmealDto getWithDish(String id);

    void saveWithDish(SetmealDto setmealDto);

    void updateWithDish(SetmealDto setmealDto);

}
