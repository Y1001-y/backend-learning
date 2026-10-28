package top.mqxu.toolkit.dto;

import java.util.List;

/**
 * 短链统计（练习 3：增加按天统计）
 *
 * @param code      短码
 * @param url       原始长链
 * @param clicks    点击次数
 * @param createdAt 创建时间戳（毫秒）
 * @param expireAt  过期时间戳（毫秒）
 * @param daily     最近 7 天的按天点击数，key 为 yyyy-MM-dd，没有点击的天补 0
 * @author mqxu
 * @date 2026-10-21
 */
public record ShortLinkStats(String code, String url, long clicks, long createdAt, long expireAt,
                             List<DailyClick> daily) {

    /**
     * 单日点击
     *
     * @param date  日期，yyyy-MM-dd
     * @param clicks 当天点击次数
     */
    public record DailyClick(String date, long clicks) {
    }
}
