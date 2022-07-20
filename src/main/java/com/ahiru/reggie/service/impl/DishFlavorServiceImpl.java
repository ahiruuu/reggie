package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.entity.DishFlavor;
import com.ahiru.reggie.mapper.DishFlavorMapper;
import com.ahiru.reggie.service.DishFlavorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
