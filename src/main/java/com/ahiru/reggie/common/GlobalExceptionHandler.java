package com.ahiru.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */

@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})  //通过AOP的方式拦截所有controller
@ResponseBody
public class GlobalExceptionHandler {

    //拦截处理"数据库唯一约束的字段重复添加失败"的异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)  //注解声明是处理异常的方法，并将异常类放进来，在控制台找
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            String msg = s[2]+"已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }


    //处理自定义的CustomException异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }

}
