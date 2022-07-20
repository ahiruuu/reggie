package com.ahiru.reggie.filter;

import com.ahiru.reggie.common.BaseContext;
import com.ahiru.reggie.common.R;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录的过滤器，未登录跳回登录页
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")  //过滤器的注解，指定名字（随便取），指定拦截的请求
public class LoginCheckFilter implements Filter {   //过滤器要实现Filter接口，重写doFilter

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        long id = Thread.currentThread().getId();
        log.info("当前线程id为"+id);

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //写过滤器逻辑

        //1、获取请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI); //Slf4j输出日志支持用占位符{}

        //2、列举不需要处理直接放行的路径
        String[] urls = new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/user/login",
            "/user/sendMsg"
        };

        //3、判断请求的URI是否需要处理
        boolean check = check(urls, requestURI);

        //4、不需要处理就放行
        if(check){
            log.info("本次请求不需要处理");
            filterChain.doFilter(request, response);
            return;
        }

        //5、需要处理的，检查用户是否已完成登录
        //5.1(后台）根据session查登录信息，若有则放行
        if(request.getSession().getAttribute("employee") != null){
            Long CurrentId = (Long) request.getSession().getAttribute("employee");
            log.info("用户已登录（id:{})，放行", CurrentId);

            BaseContext.setCurrentId(CurrentId); //将id存进当前线程的ThreadLocal
            filterChain.doFilter(request, response);
            return;
        }
        //5.2(移动端）根据session查登录信息，若有则放行
        if(request.getSession().getAttribute("user") != null){
            Long UserId = (Long) request.getSession().getAttribute("user");
            log.info("用户已登录（id:{})，放行", UserId);

            BaseContext.setCurrentId(UserId); //将id存进当前线程的ThreadLocal
            filterChain.doFilter(request, response);
            return;
        }


        //没有则返回R.error（根据前端-js/request.js
        //if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 返回登录页面
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));  //返回R转JSON
        log.info("用户未登录，重定向");
        return;
    }

    //spring提供的路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    //判断方法
    public boolean check(String[] urls, String requestURI){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if (match){return true;};
        }
        return false;
    }

}

