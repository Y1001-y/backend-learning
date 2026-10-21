package top.mqxu.mybatis.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 练习 3：每个班的平均分
 */
@Data
public class ClassAvgVO {
    private String className;
    private Long scoreCount;
    private BigDecimal avgScore;
}
