package top.mqxu.mybatis.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 练习 1：每门课的平均分、选课人数
 */
@Data
public class CourseAvgVO {
    private Long courseId;
    private String courseName;
    private Long studentCount;
    private BigDecimal avgScore;
}
