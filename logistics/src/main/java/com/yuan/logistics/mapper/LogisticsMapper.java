package com.yuan.logistics.mapper;

import com.yuan.logistics.entity.Logistics;

public interface LogisticsMapper {
    int deleteByPrimaryKey(String packageId);

    int insert(Logistics record);

    int insertSelective(Logistics record);

    Logistics selectByPrimaryKey(String packageId);

    int updateByPrimaryKeySelective(Logistics record);

    int updateByPrimaryKey(Logistics record);
}