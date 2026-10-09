package top.mqxu.config.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 应用配置（带校验）
 *
 * @author mqxu
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank(message = "应用名称 app.name 不能为空")
    private String name;

    private String author;

    @Min(value = 1, message = "app.port 必须大于等于 1")
    @Max(value = 65535, message = "app.port 必须小于等于 65535")
    private Integer port;

    @Min(1)
    @Max(1000)
    private Integer maxCount;

    @Email(message = "app.email 格式不正确")
    private String email;

    /** 改为 String 类型，避免 Date 类型转换失败 */
    private String date;
}