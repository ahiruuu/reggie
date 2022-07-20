package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.entity.OrderDetail;
import com.ahiru.reggie.mapper.OrderDetailMapper;
import com.ahiru.reggie.service.OrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
