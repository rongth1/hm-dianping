package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author 登录拦截器
 * @description: TODO
 * @date 2022/10/9
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取session
        HttpSession session = request.getSession();
        // 根据session获取其中的用户
        Object user = session.getAttribute("user");
        // 判断用户是否存在
        if (null == user) {
            // 不存在，拦截，并返回状态码 401 未授权
            response.setStatus(401);
            return false;
        }
        // 存在将其存储到ThreadLocal中
        UserHolder.saveUser((UserDTO) user);
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
