package top.mqxu.zhihu.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import top.mqxu.zhihu.common.ResponseResult;
import top.mqxu.zhihu.entity.Special;
import top.mqxu.zhihu.service.SpecialService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/special")
public class SpecialController {

    @Resource
    private SpecialService specialService;

    /** 全部专题，按更新时间降序 */
    @GetMapping("/all")
    public ResponseResult all() {
        return ResponseResult.builder()
                .code(200).msg("success")
                .data(specialService.getAll())
                .build();
    }

    /** 按分类查询 */
    @GetMapping("/category/{category}")
    public ResponseResult byCategory(@PathVariable String category) {
        return ResponseResult.builder()
                .code(200).msg("success")
                .data(specialService.getByCategory(category))
                .build();
    }

    /** 分页查询 */
    @GetMapping("/page")
    public ResponseResult page(@RequestParam(defaultValue = "3") int limit,
                               @RequestParam(defaultValue = "0") int offset) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", specialService.getByPage(limit, offset));
        result.put("total", specialService.count());
        return ResponseResult.builder()
                .code(200)
                .msg("success")
                .data(result)
                .build();
    }
}