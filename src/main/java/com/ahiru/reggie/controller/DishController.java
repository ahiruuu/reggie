package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.R;
import com.ahiru.reggie.dto.DishDto;
import com.ahiru.reggie.entity.Category;
import com.ahiru.reggie.entity.Dish;
import com.ahiru.reggie.entity.DishFlavor;
import com.ahiru.reggie.service.CategoryService;
import com.ahiru.reggie.service.DishFlavorService;
import com.ahiru.reggie.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.rmi.CORBA.Util;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;

    //菜品信息分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){  //接受三个参数，返回的是MyBatisPlus提供的Page对象
        log.info("菜品管理-请求第{}页，一页{}条，搜索关键词为{}",page,pageSize,name);

        //分页构造器，指定分页
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器，指定根据name查询（如有
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name!=null, Dish::getName, name); //condition为true时才执行查询
        //指定排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询,传入分页条件和查询条件,查询结果封装进Page对象dishPage, dishPage有categoryId，但没有categoryName
        dishService.page(dishPage, queryWrapper);

        //将查出来的dishPage基本属性拷贝到dishDtoList，并忽略records属性
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        //将dishPage的Records取出
        List<Dish> records = dishPage.getRecords();
        //根据dish的records返回dishDto的records，即List<DishDto>
        List<DishDto> collect = records.stream().map(item -> {

            //新建一个dishDto，作为最后的返回值
            DishDto dishDto = new DishDto();

            //将dish的Records中的CategoryId取出，使用categoryService查出对应的category
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            //如果查的到，再将categoryName赋给dishDto
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //将item的其他值也赋给dishDto
            BeanUtils.copyProperties(item,dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        //将List<DishDto>作为dishDtoPage的Records
        dishDtoPage.setRecords(collect);

        return R.success(dishDtoPage);
    }


    //删除,支持批量, ?ids=1397860963880316929,1397860792492666881
    @DeleteMapping()
    public R<String> delete(@RequestParam(value="ids") String ids){
        String[] idss = ids.split(",");
        dishService.removeByIds(Arrays.asList(idss));
        //还有Flavor表需要删
        for(String id : idss){
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(queryWrapper);
        }

        return R.success("分类删除成功");
    }

    //启售停售，支持批量, dish/status/1?ids=1413384757047271425,1548187189865730049
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable(name="status")int status, @RequestParam(value="ids") String ids){
        log.info("接收到参数:{},{}" ,status,ids);
        String[] idss = ids.split(",");
        for( String id : idss ){
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("启售停售修改成功");
    }


    //新增
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    //修改
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }


    //根据id查菜品信息
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable(name = "id")String id){
        log.info("根据id（{}）查菜品信息", id);

        DishDto dishDto = dishService.getWithFlavor(id);

        if(dishDto!=null){
            return R.success(dishDto);
        }
        return R.error("查询失败，未知错误");
    }


    //按分类查菜品列表(包含口味)，dish/list?categoryId=1397844263642378242&status=1，get
    @GetMapping(value="/list")
    public R<List<DishDto>> getByCategoryId(Dish dish){

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //查分类下的菜品列表
        if(dish.getCategoryId()!=null) {
            log.info("根据分类查菜品列表,分类id："+dish.getCategoryId());
            queryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        }
        if(dish.getName()!=null){
            log.info("根据名称查菜品列表,名称："+dish.getName());
            queryWrapper.like(Dish::getName, dish.getName());
        }

        queryWrapper.eq(Dish::getStatus, 1);  //只展示启售状态的菜品
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        //查口味数据
        List<DishDto> collect = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(collect);

    }

//    //按分类查菜品列表(不含口味信息)，dish/list?categoryId=1397844263642378242&status=1，get
//    @GetMapping(value="/list")
//    public R<List<DishDto>> getByCategoryId(Dish dish){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//
//        if(dish.getCategoryId()!=null) {
//            log.info("根据分类查菜品列表,分类id："+dish.getCategoryId());
//            queryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
//        }
//        if(dish.getName()!=null){
//            log.info("根据名称查菜品列表,名称："+dish.getName());
//            queryWrapper.like(Dish::getName, dish.getName());
//        }
//
//        queryWrapper.eq(Dish::getStatus, 1);  //只展示启售状态的菜品
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//
//    }

}
