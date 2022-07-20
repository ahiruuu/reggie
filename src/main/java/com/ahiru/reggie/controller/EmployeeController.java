package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.R;
import com.ahiru.reggie.entity.Employee;
import com.ahiru.reggie.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //前端请求：登录校验 /employee/login，post，包含username和password
    @PostMapping("/login")
    //@RequestBody用来将post的json数据赋值给Employee对象（注意json中的key要和对象变量名命名一致，否则无法匹配和赋值）
    //HttpServletRequest对象用来创建和取得session，存储和得知当前的登录用户
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest httpServletRequest){

        //1、对发过来的password进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、从根据username从数据库查数据
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();//myBatisPlus的查询条件对象
        lqw.eq(Employee::getUsername, employee.getUsername());//设定查询条件lqw：请求的username与数据库的username求等
        Employee result = employeeService.getOne(lqw);//使用Service层的查询方法，因为查询结果只会有一个值，所以用getOne方法，得到一个对象

        //3、判断查询结果是否为null（不存在该username
        if(result==null){
            return R.error("登录失败，用户名不存在");
        }

        //4、判断password是否匹配，如否返回R.error
        if(!result.getPassword().equals(password)){
            return R.error("登录失败，密码错误");
        }

        //5、查员工状态是否为被禁用
        if(result.getStatus()==0){
            return R.error("登录失败，账号已被禁用");
        }

        //6、登录成功，将用户id存一个session，并返回用户信息
        httpServletRequest.getSession().setAttribute("employee", result.getId());
        return R.success(result);

    }

    //前端请求：退出登录 /employee/logout，post
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest httpServletRequest){
        //清理session
        httpServletRequest.getSession().removeAttribute("employee");
        return R.success("用户信息session已清除");
    }


    //员工信息分页查询
    //员工列表：前端请求：/employee/page，get,?page=1&pageSize=10
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){  //接受三个参数，返回的是MyBatisPlus提供的Page对象
        log.info("员工管理-请求第{}页，一页{}条，搜索关键词为{}",page,pageSize,name);

        //分页构造器，指定分页
        Page list = new Page(page, pageSize);
        //条件构造器，指定根据name查询（如有
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name!=null, Employee::getName, name); //condition为true时才执行查询
        //指定排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询,传入分页条件和查询条件,查询结果封装进Page对象list
        employeeService.page(list, queryWrapper);
        return R.success(list);
    }

    //添加员工: 前端请求：/employee，post
    @PostMapping
    public R<String> addEmployee(@RequestBody Employee employee, HttpServletRequest httpServletRequest){ //将前端发送的json数据封装为Employee对象
        long id = Thread.currentThread().getId();
        log.info("当前线程id为"+id);

        log.info("新增员工信息：{}", employee.toString());

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes())); //设置初始密码并MD5加密
//        已交给公共字段自动填充，见common-MyMetaObjectHandler
//        Long currentUser = (Long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser(currentUser); //需要Long型id
//        employee.setUpdateUser(currentUser);
        boolean save = employeeService.save(employee);
        if(save){
            return R.success("1");
        }
        return R.error("添加失败");
    }


    //修改员工信息
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest httpServletRequest){
        log.info(employee.toString());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((Long)httpServletRequest.getSession().getAttribute("employee"));
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    //根据id查员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable(name = "id")String id){
        log.info("根据id（{}）查员工信息", id);
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }

}
