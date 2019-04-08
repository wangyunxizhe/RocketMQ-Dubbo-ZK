package com.yuan.order.mapper;

import com.yuan.order.entity.Order;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

public interface OrderMapper {
    int deleteByPrimaryKey(String orderId);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(String orderId);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status,
                          @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);
}