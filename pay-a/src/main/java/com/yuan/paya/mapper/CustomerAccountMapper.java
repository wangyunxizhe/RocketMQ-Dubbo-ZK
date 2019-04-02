package com.yuan.paya.mapper;

import com.yuan.paya.entity.CustomerAccount;

public interface CustomerAccountMapper {
    int deleteByPrimaryKey(String accountId);

    int insert(CustomerAccount record);

    int insertSelective(CustomerAccount record);

    CustomerAccount selectByPrimaryKey(String accountId);

    int updateByPrimaryKeySelective(CustomerAccount record);

    int updateByPrimaryKey(CustomerAccount record);
}