package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.common.BaseContext;
import com.ahiru.reggie.entity.*;
import com.ahiru.reggie.mapper.OrdersMapper;
import com.ahiru.reggie.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    //提交订单
    @Override
    public void submit(Orders orders) {
        //传递过来到信息有address_book_id、备注remark、pay_method
        //需要更新orders、order_detail、shopping_cart三张表

        //获取UserId
        Long currentId = BaseContext.getCurrentId();
        //查购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        //查用户信息
        User user = userService.getById(currentId);

        //查地址信息
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        //使用MP的IdWorker生成orderId
        long orderId = IdWorker.getId();

        //new一个购物车总金额
        AtomicInteger amount = new AtomicInteger(0); //原子操作，保证在多线程时不出错
        List<OrderDetail> orderDetails = list.stream().map(item->{

            //准备OrderDetail对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());

            //计算购物车总金额
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //准备Order对象
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setUserId(currentId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(
            addressBook.getProvinceName()   ==null?"":addressBook.getProvinceName()+
            addressBook.getCityName()       ==null?"":addressBook.getCityName()+
            addressBook.getDistrictName()   ==null?"":addressBook.getDistrictName()+
            addressBook.getDetail()         ==null?"":addressBook.getDetail()
        );
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());

        //更新Orders表
        this.save(orders);

        //更新OrderDetail表
        orderDetailService.saveBatch(orderDetails);

        //清空购物车
        shoppingCartService.remove(queryWrapper);

    }
}
