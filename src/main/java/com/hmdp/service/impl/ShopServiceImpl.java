package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.cacheUtils.CleanCache;
import com.hmdp.cacheUtils.QueryCache;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import org.springframework.stereotype.Service;

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

    @QueryCache(prefix = RedisConstants.CACHE_SHOP_KEY, field = " #id ")
    @Override
    public Result queryById(Long id) {
        Shop shopDB = getById(id);
        // 判断数据库中商铺是否存在
        if (null == shopDB) {
            // 不存在，返回404
            return Result.fail("商铺信息不存在！");
        }
        return Result.ok(shopDB);
    }


    /**
     * 更新商铺信息
     * @param shop 商铺数据
     * @return 无
     */
    @Override
    @CleanCache(prefix = RedisConstants.CACHE_SHOP_KEY, field = "#shopId")
    public Result updateShopById(long shopId, Shop shop) {
        // 修改数据库
        boolean dbFlag = updateById(shop);
        if (! dbFlag) {
            return Result.fail("店铺信息更新失败，db");
        }
        return Result.ok(shopId);
    }
}
