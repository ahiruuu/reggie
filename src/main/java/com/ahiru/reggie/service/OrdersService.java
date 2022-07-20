package com.ahiru.reggie.service;

import com.ahiru.reggie.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);

}
