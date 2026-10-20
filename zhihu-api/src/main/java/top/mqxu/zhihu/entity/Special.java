package top.mqxu.zhihu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_special")
public class Special {
    @TableId(type = IdType.INPUT)   // id 是原始 id，不用自增
    private Long id;
    private String title;
    private String introduction;     // 教程卡片简介
    private String banner;           // 教程字段：图片链接
    private Integer viewCount;       // 教程字段：浏览量
    private Integer followersCount;  // 教程字段：关注数
    private String isFollowing;      // 教程字段：关注状态 "TRUE"/"FALSE"
    private Long updated;
    private String category;         // 扩展练习1
}