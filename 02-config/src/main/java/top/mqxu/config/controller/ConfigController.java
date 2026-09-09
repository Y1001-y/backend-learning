package top.mqxu.config.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Value("${server.port}")
    private  Integer serverPort;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${mqxu.name}")
    private String myName;

    @Value("${mqxu.job}")
    private String myJob;

    @GetMapping("/basic")
    public String getBasicInfo(){
        return "服务器端口是" + this.serverPort + ",应用名称是：" + appName;
    }

    @GetMapping("/myInfo")
    public String getMyInfo(){
        return "我的名字是" + this.myName + ",职业是：" + myJob;
    }
}
