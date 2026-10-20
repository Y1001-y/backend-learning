package top.mqxu.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.mqxu.zhihu.entity.Special;

@Mapper   // 让 Spring 识别为 MyBatis Mapper
public interface SpecialMapper extends BaseMapper<Special> {
}