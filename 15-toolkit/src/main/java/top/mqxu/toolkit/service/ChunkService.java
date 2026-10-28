package top.mqxu.toolkit.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.mqxu.toolkit.common.BusinessException;
import top.mqxu.toolkit.common.ErrorCode;
import top.mqxu.toolkit.config.ToolkitProperties;
import top.mqxu.toolkit.dto.ChunkInitRequest;
import top.mqxu.toolkit.dto.ChunkInitResponse;
import top.mqxu.toolkit.dto.ChunkMergeResponse;
import top.mqxu.toolkit.dto.ChunkStatusResponse;
import top.mqxu.toolkit.support.ChunkSession;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 分片上传（练习 5：进度改用 Redis 位图）
 *
 * @author mqxu
 * @date 2026-10-21
 * @description 一片一片传；已传分片用 SETBIT upload:{id} index 1 标记，
 * BITCOUNT 算已传数量，GETBIT 判断某片是否已传，不再扫目录；
 * 位图与元数据都放 Redis，多实例部署时进度共享
 */
@Service
@RequiredArgsConstructor
public class ChunkService {

    private static final String BITMAP_PREFIX = "upload:";

    private final ToolkitProperties properties;
    private final StringRedisTemplate redis;

    private TimedCache<String, ChunkSession> cache;

    @PostConstruct
    void init() {
        cache = CacheUtil.newTimedCache(properties.getChunk().getExpireSeconds() * 1000);
        cache.schedulePrune(60_000);
        FileUtil.mkdir(new File(properties.getWorkDir(), "chunks"));
        FileUtil.mkdir(new File(properties.getWorkDir(), "uploads"));
    }

    @PreDestroy
    void destroy() {
        cache.cancelPruneSchedule();
        cache.clear();
    }

    public ChunkInitResponse init(ChunkInitRequest request) {
        String fileName = safeName(request.fileName());
        int total = request.totalChunks() == null ? 0 : request.totalChunks();
        if (total <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "totalChunks 必须大于 0");
        }

        String uploadId = IdUtil.fastSimpleUUID();
        FileUtil.mkdir(chunkDir(uploadId));

        ChunkSession session = new ChunkSession(uploadId, fileName, total,
                properties.getChunk().getChunkSize(), request.md5());
        cache.put(uploadId, session);
        // 位图随会话一起过期，新的 uploadId 位图本来就是全 0
        redis.opsForValue().setBit(bitmapKey(uploadId), Math.max(0, total - 1), false);
        redis.expire(bitmapKey(uploadId),
                Duration.ofSeconds(properties.getChunk().getExpireSeconds()));

        return new ChunkInitResponse(uploadId, session.getChunkSize(), total,
                uploadedIndexes(session));
    }

    public ChunkStatusResponse upload(String uploadId, int index, MultipartFile file) {
        ChunkSession session = get(uploadId);
        if (index < 0 || index >= session.getTotalChunks()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "分片序号超出范围：0 到 " + (session.getTotalChunks() - 1));
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        File part = new File(chunkDir(uploadId), index + ".part");
        try {
            file.transferTo(part);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "分片写入失败：" + e.getMessage());
        }
        // 练习 5：落盘成功即置位，O(1)，不扫目录
        redis.opsForValue().setBit(bitmapKey(uploadId), index, true);
        return status(uploadId);
    }

    public ChunkStatusResponse status(String uploadId) {
        ChunkSession session = get(uploadId);
        List<Integer> uploaded = uploadedIndexes(session);
        List<Integer> missing = new ArrayList<>();
        for (int i = 0; i < session.getTotalChunks(); i++) {
            if (!uploaded.contains(i)) {
                missing.add(i);
            }
        }
        return new ChunkStatusResponse(uploadId, session.getTotalChunks(), uploaded, missing);
    }

    public ChunkMergeResponse merge(String uploadId) {
        ChunkSession session = get(uploadId);
        List<Integer> uploaded = uploadedIndexes(session);
        if (uploaded.size() < session.getTotalChunks()) {
            throw new BusinessException(ErrorCode.CHUNK_NOT_COMPLETE,
                    "还缺 " + (session.getTotalChunks() - uploaded.size()) + " 片");
        }

        String fileId = IdUtil.fastSimpleUUID();
        File uploadDir = new File(properties.getWorkDir(), "uploads");
        File target = new File(uploadDir, fileId + "_" + session.getFileName());

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
            for (int i = 0; i < session.getTotalChunks(); i++) {
                File part = new File(chunkDir(uploadId), i + ".part");
                FileUtil.writeToStream(part, out);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.CHUNK_MERGE_FAILED, e.getMessage());
        }

        String md5 = DigestUtil.md5Hex(target);
        if (StrUtil.isNotBlank(session.getMd5()) && !md5.equalsIgnoreCase(session.getMd5())) {
            FileUtil.del(target);
            throw new BusinessException(ErrorCode.CHUNK_MERGE_FAILED,
                    "MD5 校验失败，文件可能在传输中损坏");
        }

        FileUtil.del(chunkDir(uploadId));
        cache.remove(uploadId);
        redis.delete(bitmapKey(uploadId));

        return new ChunkMergeResponse(fileId, session.getFileName(), target.length(), md5,
                "/toolkit/chunk/download/" + fileId);
    }

    public File file(String fileId) {
        if (StrUtil.isBlank(fileId) || !fileId.matches("[a-f0-9]{32}")) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        File dir = new File(properties.getWorkDir(), "uploads");
        File[] matches = dir.listFiles((d, name) -> name.startsWith(fileId + "_"));
        if (matches == null || matches.length == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return matches[0];
    }

    private ChunkSession get(String uploadId) {
        ChunkSession session = StrUtil.isBlank(uploadId) ? null : cache.get(uploadId, false);
        if (session == null) {
            throw new BusinessException(ErrorCode.CHUNK_SESSION_NOT_FOUND);
        }
        return session;
    }

    /**
     * 练习 5：已传分片从 Redis 位图读出。
     * 1000 片时从“1000 次 stat 调用 + 排序”变成一次 BITCOUNT + n 次 GETBIT（内存操作），
     * 也不再依赖文件系统列目录
     */
    private List<Integer> uploadedIndexes(ChunkSession session) {
        String key = bitmapKey(session.getUploadId());
        List<Integer> uploaded = new ArrayList<>();
        for (int i = 0; i < session.getTotalChunks(); i++) {
            if (Boolean.TRUE.equals(redis.opsForValue().getBit(key, i))) {
                uploaded.add(i);
            }
        }
        return uploaded;
    }

    private String bitmapKey(String uploadId) {
        return BITMAP_PREFIX + uploadId;
    }

    private File chunkDir(String uploadId) {
        return new File(properties.getWorkDir(), "chunks/" + uploadId);
    }

    /**
     * 只取文件名，挡掉 ../ 和绝对路径
     */
    private String safeName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "fileName 不能为空");
        }
        // 只留文件名本身，去掉浏览器或攻击者塞进来的路径部分
        String name = FileUtil.getName(fileName);
        if (StrUtil.isBlank(name) || name.contains("..")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件名不合法");
        }
        return name;
    }
}
