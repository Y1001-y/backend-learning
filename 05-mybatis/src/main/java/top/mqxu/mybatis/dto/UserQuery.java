package top.mqxu.mybatis.dto;

import lombok.Data;

@Data
public class UserQuery {
    private Integer gender;
    private Integer status;
    private Boolean vip;
    private String memberLevel;
    private Integer minAge;
    private Integer maxAge;
    private String keyword;
}