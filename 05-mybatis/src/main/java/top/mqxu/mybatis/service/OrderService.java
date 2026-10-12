package top.mqxu.mybatis.service;

import com.baomidou.mybatisplus.spring.service.IService;
import top.mqxu.mybatis.entity.Order;

public interface OrderService extends IService<Order> {
    // 创建订单（创建订单 + 扣库存，事务原子性）
    boolean createOrder(Order order);
}