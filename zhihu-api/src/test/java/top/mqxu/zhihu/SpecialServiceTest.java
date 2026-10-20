package top.mqxu.zhihu;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.mqxu.zhihu.entity.Special;
import top.mqxu.zhihu.service.SpecialService;

import java.util.List;

@SpringBootTest
@Slf4j
public class SpecialServiceTest {

    @Resource
    private SpecialService specialService;

    @Test
    void getAll() {
        List<Special> all = specialService.getAll();
        log.info("查询到 {} 条专题", all.size());
        all.forEach(special -> log.info("专题信息：{}", special));
    }
}