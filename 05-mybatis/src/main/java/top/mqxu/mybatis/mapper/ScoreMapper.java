package top.mqxu.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.mqxu.mybatis.entity.Score;
import top.mqxu.mybatis.vo.ClassAvgVO;
import top.mqxu.mybatis.vo.CourseAvgVO;
import top.mqxu.mybatis.vo.ScoreSheetVO;

import java.util.List;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 教程示例：一条 join SQL 查某学生的成绩单
     */
    @Select("""
            SELECT s.id    AS scoreId,
                   st.name AS studentName,
                   c.name  AS courseName,
                   s.score AS score
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
              AND s.student_id = #{studentId}
            ORDER BY s.id
            """)
    List<ScoreSheetVO> selectScoreSheet(@Param("studentId") Long studentId);

    /**
     * 练习 1：每门课的平均分，带上课程名和选课人数
     */
    @Select("""
            SELECT s.course_id            AS courseId,
                   c.name                 AS courseName,
                   COUNT(*)               AS studentCount,
                   ROUND(AVG(s.score), 2) AS avgScore
            FROM t_score s
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
            GROUP BY s.course_id, c.name
            ORDER BY s.course_id
            """)
    List<CourseAvgVO> selectCourseAvg();

    /**
     * 练习 2：成绩单分页（join 版），第一个参数传 IPage，分页插件自动拼 LIMIT 和 COUNT
     */
    @Select("""
            SELECT s.id    AS scoreId,
                   st.name AS studentName,
                   c.name  AS courseName,
                   s.score AS score
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
                     JOIN t_course c ON c.id = s.course_id
            WHERE s.deleted = 0
              AND s.student_id = #{studentId}
            ORDER BY s.id
            """)
    IPage<ScoreSheetVO> selectScoreSheetPage(IPage<ScoreSheetVO> page, @Param("studentId") Long studentId);

    /**
     * 练习 3：每个班的平均分。注意 st.deleted = 0 必须写，
     * 否则已逻辑删除的赵雪（deleted=1，成绩还在）会拉低软件2421 的平均分
     */
    @Select("""
            SELECT st.class_name          AS className,
                   COUNT(*)               AS scoreCount,
                   ROUND(AVG(s.score), 2) AS avgScore
            FROM t_score s
                     JOIN t_student st ON st.id = s.student_id AND st.deleted = 0
            WHERE s.deleted = 0
            GROUP BY st.class_name
            ORDER BY st.class_name
            """)
    List<ClassAvgVO> selectClassAvg();
}
