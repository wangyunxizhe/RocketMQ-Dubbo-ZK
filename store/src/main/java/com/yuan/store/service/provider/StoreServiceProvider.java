package com.yuan.store.service.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.yuan.store.mapper.StoreMapper;
import com.yuan.store.service.api.StoreServiceApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class StoreServiceProvider implements StoreServiceApi {

    @Autowired
    private StoreMapper storeMapper;

    /**
     * 查询数据的版本号
     *
     * @param supplierId 供应商id
     * @param goodsId    商品id
     */
    @Override
    public int selectVersion(String supplierId, String goodsId) {
        return storeMapper.selectVersion(supplierId, goodsId);
    }

    /**
     * 更新库存，版本号
     */
    @Override
    public int updateStoreCountByVersion(int version, String supplierId, String goodsId,
                                         String updateBy, Date updateTime) {
        return storeMapper.updateStoreCountByVersion(version, supplierId, goodsId,
                updateBy, updateTime);
    }

    /**
     * 查询库存
     *
     * @param supplierId 供应商id
     * @param goodsId    商品id
     */
    @Override
    public int selectStoreCount(String supplierId, String goodsId) {
        return storeMapper.selectStoreCount(supplierId, goodsId);
    }
}
