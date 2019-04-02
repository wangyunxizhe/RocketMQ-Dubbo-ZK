package com.yuan.payb.mapper;

import com.yuan.payb.entity.PlatformAccount;

public interface PlatformAccountMapper {
    int deleteByPrimaryKey(String accountId);

    int insert(PlatformAccount record);

    int insertSelective(PlatformAccount record);

    PlatformAccount selectByPrimaryKey(String accountId);

    int updateByPrimaryKeySelective(PlatformAccount record);

    int updateByPrimaryKey(PlatformAccount record);
}