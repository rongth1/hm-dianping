package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.验证手机号, 如果不符合、返回错误信息, 符合，生成验证码
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        if (phoneInvalid) {
            return Result.fail("手机号码格式错误！");
        }
        // 2. 生成验证码，并将验证码保存到redis中
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.SECONDS);

        // 3. 发送验证码
        log.debug("发送短信验证码成功，验证码为：{}", code);
        return Result.ok();
    }


    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 校验手机号
        String phone = loginForm.getPhone();
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        if (phoneInvalid) {
            return Result.fail("手机号码格式错误！");
        }
        // todo 从redis中获取验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (null == cacheCode || !cacheCode.equals(code)) {
            // 不一致报错
            return Result.fail("验证码错误！");
        }
        // 一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 判断用户是否存在，不存在创建新用户保存。
        if (null == user) {
            user = createUserWithPhone(phone);
        }

        // 保存用户信息到session
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    /**
     *  根据手机号去创建一个用户
     * @param phone 手机号
     * @return  新创建的用户
     */
    private User createUserWithPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 保存用户
        save(user);
        return user;
    }

}
