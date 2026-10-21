package top.mqxu.mybatis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mqxu.mybatis.entity.Student;
import top.mqxu.mybatis.service.StudentService;
import top.mqxu.mybatis.vo.ClassAvgVO;
import top.mqxu.mybatis.vo.CourseAvgVO;
import top.mqxu.mybatis.vo.ScoreSheetVO;

import java.util.List;

/**
 * 学生成绩接口：教程案例 + 三道练习题
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 全部学生（5 条，已删除的赵雪不出现在结果里）
     */
    @GetMapping
    public List<Student> listAllStudents() {
        return studentService.listAll();
    }

    /**
     * 练习 1：每门课的平均分、选课人数
     */
    @GetMapping("/course-avg")
    public List<CourseAvgVO> getCourseAvg() {
        return studentService.courseAvg();
    }

    /**
     * 练习 3：每个班的平均分
     */
    @GetMapping("/class-avg")
    public List<ClassAvgVO> getClassAvg() {
        return studentService.classAvg();
    }

    /**
     * 某个学生的成绩单。mode 传 steps 走分步查询，传 join 走一条 SQL
     */
    @GetMapping("/{id}/scores")
    public List<ScoreSheetVO> getScoreSheet(@PathVariable Long id,
                                     @RequestParam(defaultValue = "steps") String mode) {
        return "join".equals(mode)
                ? studentService.scoreSheetByJoin(id)
                : studentService.scoreSheetBySteps(id);
    }

    /**
     * 练习 2：成绩单（join 版）分页
     */
    @GetMapping("/{id}/scores/page")
    public IPage<ScoreSheetVO> getScoreSheetPage(@PathVariable Long id,
                                          @RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "2") long size) {
        return studentService.scoreSheetPage(id, current, size);
    }

    /**
     * 按 id 查一个学生，放在最后，避免抢走上面的具体路径
     */
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentService.getStudent(id);
    }
}
