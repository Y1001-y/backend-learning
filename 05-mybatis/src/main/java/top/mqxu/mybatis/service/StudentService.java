package top.mqxu.mybatis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import top.mqxu.mybatis.entity.Student;
import top.mqxu.mybatis.vo.ClassAvgVO;
import top.mqxu.mybatis.vo.CourseAvgVO;
import top.mqxu.mybatis.vo.ScoreSheetVO;

import java.util.List;

public interface StudentService extends IService<Student> {

    /**
     * 按 id 查一个学生
     */
    Student getStudent(Long id);

    /**
     * 查全部学生（自动过滤逻辑删除）
     */
    List<Student> listAll();

    /**
     * 某学生的成绩单：分步单表查询，在 Java 里拼
     */
    List<ScoreSheetVO> scoreSheetBySteps(Long studentId);

    /**
     * 某学生的成绩单：一条 join SQL
     */
    List<ScoreSheetVO> scoreSheetByJoin(Long studentId);

    /**
     * 练习 1：每门课的平均分、选课人数
     */
    List<CourseAvgVO> courseAvg();

    /**
     * 练习 2：成绩单（join 版）分页
     */
    IPage<ScoreSheetVO> scoreSheetPage(Long studentId, long current, long size);

    /**
     * 练习 3：每个班的平均分
     */
    List<ClassAvgVO> classAvg();
}
