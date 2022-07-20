package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.entity.User;
import com.ahiru.reggie.mapper.UserMapper;
import com.ahiru.reggie.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
