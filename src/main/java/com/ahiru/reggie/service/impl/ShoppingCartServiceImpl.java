package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.entity.ShoppingCart;
import com.ahiru.reggie.mapper.ShoppingCartMapper;
import com.ahiru.reggie.service.ShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
