package top.mqxu.config.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mqxu.config.properties.StudentProperties;

/**
 * 学生信息接口
 *
 * @author mqxu
 */
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentProperties studentProperties;

    @GetMapping("/info")
    public StudentProperties getStudentInfo() {
        return studentProperties;
    }
}