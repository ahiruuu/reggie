package com.ahiru.reggie.common;

//是基于ThreadLocal封装的工具类，用来读写ThreadLocal
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
