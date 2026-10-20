package top.mqxu.zhihu.service;

import top.mqxu.zhihu.entity.Special;

import java.util.List;

public interface SpecialService {
    List<Special> getAll();
    List<Special> getByCategory(String category);
    List<Special> getByPage(int limit, int offset);
    long count();
}