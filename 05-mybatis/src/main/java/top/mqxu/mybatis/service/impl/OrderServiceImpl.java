package top.mqxu.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import top.mqxu.mybatis.entity.Goods;
import top.mqxu.mybatis.entity.Order;
import top.mqxu.mybatis.mapper.GoodsMapper;
import top.mqxu.mybatis.mapper.OrderMapper;
import top.mqxu.mybatis.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(Order order) {
        // 1. 查商品
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在：id=" + order.getGoodsId());
        }

        // 2. 库存检查：不够则抛异常，触发事务回滚
        int quantity = order.getQuantity();
        if (goods.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + goods.getStock());
        }

        // 3. 计算金额
        order.setGoodsName(goods.getName());
        order.setAmount(goods.getPrice().multiply(BigDecimal.valueOf(quantity)));

        // ★ 手动补值：自动填充不生效，必须手动
        order.setDeleted(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 4. 创建订单
        this.save(order);

        // 5. 扣库存（库存 - quantity）
        int ok = goodsMapper.update(null, new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goods.getId())
                .ge(Goods::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .set(Goods::getUpdateTime, LocalDateTime.now()));
        if (ok == 0) {
            throw new RuntimeException("扣库存失败，库存不足");
        }
        return true;
    }
}