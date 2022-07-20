package com.ahiru.reggie.service;

import com.ahiru.reggie.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {

    //自定义一个remove方法
    void checkAndRemove(Long id);

}
