package top.mqxu.logging.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")

public class ProdEnvService implements EnvService {
    @Override
    public String envInfo() {
        return "当前环境：生产环境(prod)";
    }
}