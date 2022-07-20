package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.R;
import com.ahiru.reggie.entity.Category;
import com.ahiru.reggie.entity.Employee;
import com.ahiru.reggie.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //添加分类
    @PostMapping
    public R<String> addCategory(@RequestBody Category category){
        log.info("新增分类："+category);
        boolean save = categoryService.save(category);
        if(save){
            return R.success("添加分类成功");
        }
        return R.error("添加分类失败，未知错误");
    }

    //分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){  //接受2个参数，返回的是MyBatisPlus提供的Page对象
        log.info("分类管理-请求第{}页，一页{}条",page,pageSize);

        //分页构造器，指定分页
        Page list = new Page(page, pageSize);
        //条件构造器，指定根据name查询（如有
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //指定排序条件
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询,传入分页条件和查询条件,查询结果封装进Page对象list
        categoryService.page(list, queryWrapper);
        return R.success(list);
    }

    //根据id删除
    @DeleteMapping()
    public R<String> deleteById(@RequestParam(value="ids") Long id){
        log.info(id.toString());
        categoryService.checkAndRemove(id);
        return R.success("分类删除成功");
    }

    //修改分类信息
    @PutMapping
    public R<String> update(@RequestBody Category category, HttpServletRequest httpServletRequest){
        log.info(category.toString());
        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }


//    前端请求：新增/修改菜品页面请求分类信息
//    url: '/category/list',
//    method: 'get',
//    params: 'type'=1
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，有type且与参数一致
        queryWrapper.eq(category.getType()!=null, Category::getType, category.getType());
        //添加排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
