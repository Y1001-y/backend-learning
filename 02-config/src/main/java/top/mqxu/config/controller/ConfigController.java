package top.mqxu.config.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mqxu.config.properties.AppProperties;
import top.mqxu.config.service.EnvService;

/**
 * 配置管理接口
 *
 * @author mqxu
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final AppProperties appProperties;
    private final EnvService envService;

    // ========== 第一组：原来的接口保留 ==========

    @Value("${server.port}")
    private Integer serverPort;

    @Value("${mqxu.name}")
    private String myName;

    // ① 占位符引用
    @Value("${app.author}")
    private String author;

    // ② 默认值
    @Value("${app.remark:暂无备注}")
    private String remark;

    // ③ 随机值
    @Value("${random.uuid}")
    private String randomUuid;

    @Value("${random.int(1,100)}")
    private Integer randomInt;

    // ④ SpEL 表达式
    @Value("#{${student.age} >= 18 ? '成年' : '未成年'}")
    private String adult;

    @GetMapping("/value")
    public String getValueCases() {
        return "author=" + author
                + "；remark=" + remark
                + "；randomUuid=" + randomUuid
                + "；randomInt=" + randomInt
                + "；adult=" + adult;
    }

    @GetMapping("/app")
    public AppProperties getApp() {
        return appProperties;
    }

    @GetMapping("/env")
    public String getEnv() {
        return envService.envInfo();
    }

    // ========== 第二组：新加接口，用于演示新增的 email 和 date ==========

    @GetMapping("/myInfo")
    public String getMyInfo() {
        return "myInfo";
    }

    // ========== 第三组：接口小结——返回所有 @Value 用法 ==========

    @Value("${app.email}")
    private String appEmail;

    @Value("${app.date}")
    private String appDate;

    /**
     * 综合展示 @Value 四种用法
     */
    @GetMapping("/all")
    public String getAllValueCases() {
        return "【占位符引用】author=" + author
                + "\n【默认值】remark=" + remark
                + "\n【随机值】uuid=" + randomUuid + "，int=" + randomInt
                + "\n【SpEL】adult=" + adult
                + "\n【新增校验属性】email=" + appEmail + "，date=" + appDate;
    }
}