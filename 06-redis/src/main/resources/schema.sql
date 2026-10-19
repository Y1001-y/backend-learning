# 创建数据库，如果不存在的话
CREATE DATABASE IF NOT EXISTS backend_learning DEFAULT CHARSET utf8mb4;
# 使用该数据库
USE backend_learning;

DROP TABLE IF EXISTS t_goods;

CREATE TABLE `t_goods` (
                           `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                           `name` varchar(100) NOT NULL COMMENT '商品名称',
                           `category` varchar(32) NOT NULL COMMENT '分类',
                           `price` decimal(10,2) NOT NULL COMMENT '价格',
                           `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
                           `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
                           `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1 上架，0 下架',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';