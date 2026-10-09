package top.mqxu.logging.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import top.mqxu.logging.service.EnvService;

@Service
@Profile("dev")
public class DevEnvService implements EnvService {

    @Override
    public String envInfo() {
        return "开发环境(dev)";
    }
}