package top.mqxu.mybatis.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成绩单：某学生的姓名、课程名、分数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSheetVO {
    private Long scoreId;
    private String studentName;
    private String courseName;
    private BigDecimal score;
}
