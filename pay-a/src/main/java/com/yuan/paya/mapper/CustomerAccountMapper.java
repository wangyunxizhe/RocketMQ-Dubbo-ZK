package com.yuan.paya.mapper;

import com.yuan.paya.entity.CustomerAccount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;

public interface CustomerAccountMapper {
    int deleteByPrimaryKey(String accountId);

    int insert(CustomerAccount record);

    int insertSelective(CustomerAccount record);

    CustomerAccount selectByPrimaryKey(String accountId);

    int updateByPrimaryKeySelective(CustomerAccount record);

    int updateByPrimaryKey(CustomerAccount record);

    int updateNewMoney(@Param("accountId") String accountId, @Param("newMoney") BigDecimal newMoney,
                       @Param("oldVersion") int oldVersion, @Param("updateTime") Date updateTime);
}