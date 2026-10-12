package top.mqxu.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.mqxu.mybatis.entity.Course;
import top.mqxu.mybatis.service.CourseService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 批量新增：手动补 deleted / createTime / updateTime（因自动填充未生效）
    @PostMapping("/batch")
    public boolean batchSave(@RequestBody List<Course> courses) {
        LocalDateTime now = LocalDateTime.now();
        for (Course c : courses) {
            c.setDeleted(0);             // 补 deleted=0，避免 NOT NULL 报错
            c.setCreateTime(now);        // 补 createTime
            c.setUpdateTime(now);        // 补 updateTime
            courseService.save(c);       // 逐条插入
        }
        return true;
    }

    // 批量删除（逻辑删除，deleted 置 1）
    @DeleteMapping("/batch")
    public boolean batchRemove(@RequestParam List<Long> ids) {
        return courseService.removeByIds(ids);
    }
}