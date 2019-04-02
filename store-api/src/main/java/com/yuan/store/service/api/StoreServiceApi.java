package com.yuan.store.service.api;

import java.util.Date;

/**
 * dubbo库存服务--api
 */
public interface StoreServiceApi {

    /**
     * 查询数据的版本号
     *
     * @param supplierId 供应商id
     * @param goodsId    商品id
     */
    int selectVersion(String supplierId, String goodsId);

    /**
     * 更新库存，版本号
     */
    int updateStoreCountByVersion(int version, String supplierId, String goodsId, String updateBy, Date updateTime);

    /**
     * 查询库存
     *
     * @param supplierId 供应商id
     * @param goodsId    商品id
     */
    int selectStoreCount(String supplierId, String goodsId);

}
