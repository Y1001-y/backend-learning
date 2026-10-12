package top.mqxu.mybatis.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import top.mqxu.mybatis.entity.Course;
import top.mqxu.mybatis.mapper.CourseMapper;
import top.mqxu.mybatis.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {
}