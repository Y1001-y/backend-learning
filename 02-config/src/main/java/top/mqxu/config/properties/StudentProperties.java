package top.mqxu.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 学生配置
 *
 * @author mqxu
 */
@Data
@Component
@ConfigurationProperties(prefix = "student")
public class StudentProperties {

    private String name;

    private Integer age;

    /** 简单 List */
    private List<String> hobbies;

    /** Map */
    private Map<String, Integer> scores;

    /** 嵌套对象 */
    private Address address;

    /** List 嵌套对象 */
    private List<Course> courses;

    @Data
    public static class Address {
        private String province;
        private String city;
    }

    @Data
    public static class Course {
        private String name;
        private Integer credit;
    }
}