package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        String redisKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1. 根据id从redis查询商铺缓存。（缓存中的key需要设计的合理，返回的是shop的JSON字符串）
        String shopJson = stringRedisTemplate.opsForValue().get(redisKey);
        // 2. 判断缓存是否命中
        if (StrUtil.isNotBlank(shopJson)) {
            // 2.1 命中返回商铺信息
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 3.未命中时，查询数据库
        Shop shopDB = getById(id);
        // 4. 判断数据库中商铺是否存在
        if (null == shopDB) {
            // 4.1 不存在，返回404
            return Result.fail("商铺信息不存在！");
        }
        // 4.2 存在，商铺数据写入redis缓存，返回商铺信息
        stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(shopDB));
        return Result.ok(shopDB);
    }
}
