package top.mqxu.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.mqxu.mybatis.entity.User;


@Mapper
public interface UserMapper extends BaseMapper<User> {
}