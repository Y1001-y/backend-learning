package top.mqxu.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.mqxu.mybatis.entity.Order;
import top.mqxu.mybatis.service.OrderService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单 + 扣库存（事务原子性）
     * 成功后返回 true
     */
    @PostMapping("/create")
    public boolean create(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    /**
     * 逻辑删除订单（演示 @TableLogic）
     * DELETE 实际变为 UPDATE t_order SET deleted=1
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return orderService.removeById(id);
    }
}