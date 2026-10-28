package top.mqxu.toolkit.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mqxu.toolkit.common.BusinessException;
import top.mqxu.toolkit.common.ErrorCode;
import top.mqxu.toolkit.config.ToolkitProperties;
import top.mqxu.toolkit.dto.ShortLinkResponse;
import top.mqxu.toolkit.dto.ShortLinkStats;
import top.mqxu.toolkit.support.ShortLink;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 短链服务
 *
 * @author mqxu
 * @date 2026-10-21
 * @description 随机短码 + 本地缓存 + 点击计数；练习 3：按天统计最近 7 天点击
 */
@Service
@RequiredArgsConstructor
public class ShortLinkService {

    /**
     * 去掉容易看混的 0O1lI
     */
    private static final String ALPHABET =
            "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY = 5;
    private static final int STATS_DAYS = 7;
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ToolkitProperties properties;

    private TimedCache<String, ShortLink> cache;

    /**
     * 练习 3：短码 -> (日期 -> 当天点击数)。
     * 跨天处理：点击发生在哪一天就记到哪一天的桶里，桶在点击时按当时日期创建，
     * 查询时按“最近 7 天”取桶并给没有数据的天补 0，只保留最近 7 天的桶防内存膨胀
     */
    private final Map<String, Map<String, AtomicLong>> dailyClicks = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        cache = CacheUtil.newTimedCache(properties.getShortLink().getExpireSeconds() * 1000);
        cache.schedulePrune(60_000);
    }

    @PreDestroy
    void destroy() {
        cache.cancelPruneSchedule();
        cache.clear();
    }

    public ShortLinkResponse create(String url, Long ttlSeconds) {
        if (!Validator.isUrl(url)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "url 不是合法的地址");
        }
        long ttl = ttlSeconds == null || ttlSeconds <= 0
                ? properties.getShortLink().getExpireSeconds()
                : ttlSeconds;

        String code = allocateCode();
        long expireAt = System.currentTimeMillis() + ttl * 1000;
        cache.put(code, new ShortLink(code, url, expireAt), ttl * 1000);

        String baseUrl = StrUtil.removeSuffix(properties.getShortLink().getBaseUrl(), "/");
        return new ShortLinkResponse(code, baseUrl + "/toolkit/s/" + code, url, expireAt);
    }

    /**
     * 解析短码，点一次算一次，同时记入当天的统计桶
     */
    public String resolve(String code) {
        ShortLink link = cache.get(code, false);
        if (link == null) {
            throw new BusinessException(ErrorCode.SHORT_LINK_NOT_FOUND);
        }
        link.getClicks().incrementAndGet();
        // 练习 3：按点击发生时的日期记账，跨天自然落进新的桶
        String day = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DAY_FORMAT);
        dailyClicks.computeIfAbsent(code, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(day, k -> new AtomicLong())
                .incrementAndGet();
        return link.getUrl();
    }

    public ShortLinkStats stats(String code) {
        ShortLink link = cache.get(code, false);
        if (link == null) {
            throw new BusinessException(ErrorCode.SHORT_LINK_NOT_FOUND);
        }
        return new ShortLinkStats(link.getCode(), link.getUrl(), link.getClicks().get(),
                link.getCreatedAt(), link.getExpireAt(), lastSevenDays(code));
    }

    /**
     * 练习 3：最近 7 天（含今天）的按天点击，没有数据的天补 0；
     * 顺手清掉 7 天前的旧桶，防止 Map 无限增长
     */
    private List<ShortLinkStats.DailyClick> lastSevenDays(String code) {
        Map<String, AtomicLong> buckets = dailyClicks.getOrDefault(code, Map.of());
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        List<ShortLinkStats.DailyClick> result = new ArrayList<>(STATS_DAYS);
        for (int i = STATS_DAYS - 1; i >= 0; i--) {
            String day = today.minusDays(i).format(DAY_FORMAT);
            long count = buckets.containsKey(day) ? buckets.get(day).get() : 0L;
            result.add(new ShortLinkStats.DailyClick(day, count));
        }
        // 清理 7 天窗口之外的旧数据
        String earliest = today.minusDays(STATS_DAYS - 1).format(DAY_FORMAT);
        buckets.keySet().removeIf(day -> day.compareTo(earliest) < 0);
        return result;
    }

    /**
     * 生成一个没被占用的短码
     */
    private String allocateCode() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = RandomUtil.randomString(ALPHABET, CODE_LENGTH);
            if (!cache.containsKey(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "短码分配失败，请重试");
    }
}
