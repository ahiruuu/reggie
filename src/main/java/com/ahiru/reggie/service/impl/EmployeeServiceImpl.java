package com.ahiru.reggie.service.impl;

import com.ahiru.reggie.entity.Employee;
import com.ahiru.reggie.mapper.EmployeeMapper;
import com.ahiru.reggie.service.EmployeeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{

}
