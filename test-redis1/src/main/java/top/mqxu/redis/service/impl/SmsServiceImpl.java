package top.mqxu.redis.service.impl;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.cloopen.rest.sdk.BodyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mqxu.redis.config.CloopenConfig;
import top.mqxu.redis.service.SmsService;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final CloopenConfig cloopenConfig;

    @Override
    public boolean sendSms(String phone) {
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(cloopenConfig.getServerIp(), cloopenConfig.getPort());
        sdk.setAccount(cloopenConfig.getAccountSID(), cloopenConfig.getAccountToken());
        sdk.setAppId(cloopenConfig.getAppId());
        sdk.setBodyType(BodyType.Type_JSON);

        String templateId = cloopenConfig.getTemplateId();

        // 随机四位整数
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        String[] codes = {code};

        HashMap<String, Object> result = sdk.sendTemplateSMS(phone, templateId, codes);

        if ("000000".equals(result.get("statusCode"))) {
            // 正常返回输出 data 包体信息 (map)
            HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for (String key : keySet) {
                Object object = data.get(key);
                log.info("key = {}", object);
            }
            return true;
        }
        // 异常返回输出错误码和错误信息
        log.info("错误码: {}，错误信息: {}", result.get("statusCode"), result.get("statusMsg"));
        return false;
    }
}