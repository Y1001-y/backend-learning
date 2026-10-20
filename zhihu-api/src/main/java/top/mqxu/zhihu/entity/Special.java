package top.mqxu.zhihu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_special")
public class Special {
    @TableId(type = IdType.INPUT)   // id 是 CSV 里的原始 id，不用自增
    private Long id;
    private String title;
    private String introduction;
    private Long updated;
    private String category;
    private Integer isFollowed;
}