package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.CustomException;
import com.ahiru.reggie.common.R;
import com.ahiru.reggie.dto.DishDto;
import com.ahiru.reggie.dto.SetmealDto;
import com.ahiru.reggie.entity.*;
import com.ahiru.reggie.service.CategoryService;
import com.ahiru.reggie.service.SetmealDishService;
import com.ahiru.reggie.service.SetmealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;

    //套餐信息分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){  //接受三个参数，返回的是MyBatisPlus提供的Page对象
        log.info("套餐管理-请求第{}页，一页{}条，搜索关键词为{}",page,pageSize,name);

        //分页构造器，指定分页
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器，指定根据name查询（如有
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name!=null, Setmeal::getName, name); //condition为true时才执行查询
        //指定排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询,传入分页条件和查询条件,查询结果封装进Page对象setmealPage, setmealPage有categoryId，但没有categoryName
        setmealService.page(setmealPage, queryWrapper);

        //将查出来的setmealPage基本属性拷贝到setmealDto，并忽略records属性
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        //将setmealPage的Records取出
        List<Setmeal> records = setmealPage.getRecords();
        //根据setmeal的records返回setmealDto的records，即List<SetmealDto>
        List<SetmealDto> collect = records.stream().map(item -> {

            //新建一个setmealDto，作为最后的返回值
            SetmealDto setmealDto = new SetmealDto();

            //将setmeal的Records中的CategoryId取出，使用categoryService查出对应的category
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            //如果查的到，再将categoryName赋给dishDto
            if(category!=null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            //将item的其他值也赋给dishDto
            BeanUtils.copyProperties(item,setmealDto);
            return setmealDto;
        }).collect(Collectors.toList());

        //将List<DishDto>作为dishDtoPage的Records
        setmealDtoPage.setRecords(collect);

        return R.success(setmealDtoPage);
    }

    //根据id查套餐信息
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable(name = "id")String id){
        log.info("根据id（{}）查套餐信息", id);

        SetmealDto setmealDto = setmealService.getWithDish(id);

        if(setmealDto!=null){
            return R.success(setmealDto);
        }
        return R.error("查询失败，未知错误");
    }

    //新增
    @PostMapping
    @CacheEvict(value="setmealCache", allEntries=true) //删除setmealCache下的所有缓存
    public R<String> save(@RequestBody SetmealDto setmealDto){

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    //修改
    @PutMapping
    @CacheEvict(value="setmealCache", allEntries=true) //删除setmealCache下的所有缓存
    public R<String> update(@RequestBody SetmealDto setmealDto){

        setmealService.updateWithDish(setmealDto);

        return R.success("修改套餐成功");
    }


    //删除,支持批量, ?ids=1397860963880316929,1397860792492666881
    @DeleteMapping()
    @CacheEvict(value="setmealCache", allEntries=true) //删除setmealCache下的所有缓存
    @Transactional //应该写到service里比较好，懒得改了
    public R<String> delete(@RequestParam List<String> ids){  //自动封装为list
        //先查询套餐状态，只有停售的可以删除，否则抛出业务异常
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Setmeal::getId, ids); //查出setmeal列表
        wrapper.eq(Setmeal::getStatus, 1);
        int count = setmealService.count(wrapper); //查启售的有几个
        if(count>0){
            throw new CustomException("启售中的套餐不能删除");
        }

        //删除setmeal
        setmealService.removeByIds(ids);

        //还有setmealDish表需要删
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);

        return R.success("套餐删除成功");
    }

    //启售停售，支持批量, dish/status/1?ids=1413384757047271425,1548187189865730049
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable(name="status")int status, @RequestParam(value="ids") String ids){
        log.info("接收到参数:{},{}" ,status,ids);
        String[] idss = ids.split(",");
        for( String id : idss ){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("启售停售修改成功");
    }


    //按分类查套餐列表，setmeal/list?categoryId=1397844263642378242&status=1，get
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId+'_'+#setmeal.status") //SpringCache缓存注解
    @GetMapping(value="/list")
    public R<List<Setmeal>> getByCategoryId(Setmeal setmeal){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        log.info("根据分类查套餐列表,分类id："+setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus, 1);  //只展示启售状态的菜品
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);

    }
}
