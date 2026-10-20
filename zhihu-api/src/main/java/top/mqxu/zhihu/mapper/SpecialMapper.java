package top.mqxu.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.mqxu.zhihu.entity.Special;

import java.util.List;

@Mapper
public interface SpecialMapper extends BaseMapper<Special> {

    /** 分页查询，按更新时间降序（教程要求的方法） */
    @Select("SELECT * FROM t_special ORDER BY updated DESC LIMIT #{limit} OFFSET #{offset}")
    List<Special> selectByPage(int limit, int offset);
}