package top.mqxu.mybatis.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mqxu.mybatis.entity.Course;
import top.mqxu.mybatis.entity.Score;
import top.mqxu.mybatis.entity.Student;
import top.mqxu.mybatis.mapper.CourseMapper;
import top.mqxu.mybatis.mapper.ScoreMapper;
import top.mqxu.mybatis.mapper.StudentMapper;
import top.mqxu.mybatis.service.StudentService;
import top.mqxu.mybatis.vo.ClassAvgVO;
import top.mqxu.mybatis.vo.CourseAvgVO;
import top.mqxu.mybatis.vo.ScoreSheetVO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final ScoreMapper scoreMapper;

    private final CourseMapper courseMapper;

    @Override
    public Student getStudent(Long id) {
        // selectById 自动拼接 WHERE id = ? AND deleted = 0
        return getById(id);
    }

    @Override
    public List<Student> listAll() {
        // 不带条件查全部，自动带上 deleted = 0，已删除的赵雪查不出来
        return list();
    }

    @Override
    public List<ScoreSheetVO> scoreSheetBySteps(Long studentId) {
        // 第一步：查学生本人，拿到姓名；被逻辑删除则返回空列表
        Student student = getById(studentId);
        if (student == null) {
            return List.of();
        }

        // 第二步：查这个学生的所有成绩记录
        List<Score> scores = scoreMapper.selectList(Wrappers.<Score>lambdaQuery()
                .eq(Score::getStudentId, studentId)
                .orderByAsc(Score::getId));
        if (scores.isEmpty()) {
            return List.of();
        }

        // 第三步：收集课程 id，一次 IN 查询查回课程名，避免循环里一条条查（N+1）
        Set<Long> courseIds = scores.stream()
                .map(Score::getCourseId)
                .collect(Collectors.toSet());
        Map<Long, String> courseNameMap = courseMapper.selectByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));

        // 第四步：在内存里拼成成绩单
        return scores.stream()
                .map(score -> new ScoreSheetVO(
                        score.getId(),
                        student.getName(),
                        courseNameMap.getOrDefault(score.getCourseId(), "课程不存在"),
                        score.getScore()))
                .toList();
    }

    @Override
    public List<ScoreSheetVO> scoreSheetByJoin(Long studentId) {
        return scoreMapper.selectScoreSheet(studentId);
    }

    @Override
    public List<CourseAvgVO> courseAvg() {
        return scoreMapper.selectCourseAvg();
    }

    @Override
    public IPage<ScoreSheetVO> scoreSheetPage(Long studentId, long current, long size) {
        return scoreMapper.selectScoreSheetPage(new Page<>(current, size), studentId);
    }

    @Override
    public List<ClassAvgVO> classAvg() {
        return scoreMapper.selectClassAvg();
    }
}
