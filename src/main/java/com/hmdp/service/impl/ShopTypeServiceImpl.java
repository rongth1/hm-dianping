package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.cacheUtils.QueryCache;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @QueryCache(prefix = RedisConstants.CACHE_SHOP_TYPE_KEY)
    @Override
    public Result queryList() {
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        // 判断数据库是否命中
        if (null == shopTypeList || shopTypeList.size() <= 0) {
            // 数据库数据为空，返回404
            return Result.fail("商铺类型不存在！");
        }
        return Result.ok(shopTypeList);
    }

}
