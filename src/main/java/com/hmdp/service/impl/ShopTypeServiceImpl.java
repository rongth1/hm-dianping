package com.hmdp.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        // 1. 查redis缓存
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
        // 2. 判断缓存是否命中
        if (null != shopTypeJsonList && shopTypeJsonList.size() > 0) {
            // 2.1 命中直接返回
            List<ShopType> list = shopTypeJsonList.stream().map(d -> JSONUtil.toBean(d, ShopType.class)).collect(Collectors.toList());
            return Result.ok(list);
        }
        // 3.未命中，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        // 4. 判断数据库是否命中
        if (null == shopTypeList || shopTypeList.size() <= 0) {
            // 4.1 数据库数据为空，返回404
            return Result.fail("商铺类型不存在！");
        }
        // 5. 数据库可以查到，将数据缓存到redis，并返回结果数据
        List<String> shopTypeJsonListDB = shopTypeList.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, shopTypeJsonListDB);
        // 为缓存添加过期时间
        stringRedisTemplate.expire(RedisConstants.CACHE_SHOP_TYPE_KEY, RedisConstants.CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        return Result.ok(shopTypeList);
    }
}
