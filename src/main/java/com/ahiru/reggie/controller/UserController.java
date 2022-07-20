package com.ahiru.reggie.controller;

import com.ahiru.reggie.common.R;
import com.ahiru.reggie.entity.User;
import com.ahiru.reggie.service.UserService;
import com.ahiru.reggie.utils.SMSUtils;
import com.ahiru.reggie.utils.ValidateCodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    //发送手机验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        //获取手机号
        String phone = user.getPhone();
        if(phone!=null){
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("生成验证码："+code);
            //发送短信
            //SMSUtils.sendMessage("瑞吉外卖","这里是templateCode",phone,code);

//            //保存验证码到session
//            session.setAttribute(phone,code);

            //优化：将生成的验证码缓存到Redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            Boolean aBoolean = redisTemplate.hasKey(phone);

            return R.success("手机验证码发送成功"+aBoolean);
        }

        return R.error("手机验证码发送失败");
    }

    //验证验证码
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info("验证码键值对："+ map);

        //获取手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

//        //获取session里的code
//        Object codeInSession = session.getAttribute(phone).toString();

        //优化：从Redis中取出验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);


        //比对验证码
        if(codeInSession!=null && codeInSession.equals(code)){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            //若为新用户，自动注册
            if(user==null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //保存用户信息到session
            session.setAttribute("user", user.getId());

            //删除缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("验证码错误");
    }

}
