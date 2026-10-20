package top.mqxu.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.mqxu.zhihu.entity.Special;
import top.mqxu.zhihu.mapper.SpecialMapper;
import top.mqxu.zhihu.service.SpecialService;

import java.util.List;

@Service
public class SpecialServiceImpl implements SpecialService {

    @Resource
    private SpecialMapper specialMapper;

    @Override
    public List<Special> getAll() {
        return specialMapper.selectList(
                new LambdaQueryWrapper<Special>()
                        .orderByDesc(Special::getUpdated));
    }

    @Override
    public List<Special> getByCategory(String category) {
        return specialMapper.selectList(
                new LambdaQueryWrapper<Special>()
                        .eq(Special::getCategory, category)
                        .orderByDesc(Special::getUpdated));
    }

    @Override
    public List<Special> getByPage(int limit, int offset) {
        // 教程要求的 Mapper 方法
        return specialMapper.selectByPage(limit, offset);
    }

    @Override
    public long count() {
        return specialMapper.selectCount(null);
    }
}