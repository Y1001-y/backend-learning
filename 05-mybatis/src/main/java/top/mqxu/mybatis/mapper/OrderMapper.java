package top.mqxu.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.mqxu.mybatis.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}