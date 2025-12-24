package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;
import com.koreait.moviesite.RankingGenreBoard.repository.BoardPostImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

@Service
public class BoardImageServiceImpl implements BoardImageService {

    private static final Logger log = LoggerFactory.getLogger(BoardImageServiceImpl.class);

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif",
            "image/avif", ".avif"
    );

    private final BoardPostImageRepository repo;

    @Value("${app.upload.board-dir:uploads/board}")
    private String uploadDir;

    public BoardImageServiceImpl(BoardPostImageRepository repo) {
        this.repo = repo;
    }

    @Override
    public ImageSaveResult saveImages(Long postId, List<MultipartFile> images, int maxToSave) {
        if (postId == null) return new ImageSaveResult(0, 0, List.of("postId가 없어 이미지 저장을 건너뜀"));
        if (images == null || images.isEmpty()) return new ImageSaveResult(0, 0, List.of());

        List<MultipartFile> filtered = images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        int attempted = filtered.size();
        if (attempted == 0) return new ImageSaveResult(0, 0, List.of());

        int limit = Math.max(0, Math.min(maxToSave, MAX_COUNT));

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new RuntimeException("업로드 폴더 생성 실패: " + dir, e);
        }

        int saved = 0;
        List<String> warnings = new ArrayList<>();

        int currentMaxOrder = 0;
        try {
            currentMaxOrder = repo.findMaxDisplayOrderByPostId(postId);
        } catch (Exception e) {
            log.warn("[BoardImage] findMaxDisplayOrderByPostId failed. postId={} (maybe display_order not migrated yet)", postId, e);
            currentMaxOrder = 0;
        }

        for (MultipartFile f : filtered) {
            String original = StringUtils.cleanPath(Objects.toString(f.getOriginalFilename(), ""));
            if (original.isBlank()) original = "image";

            if (saved >= limit) {
                warnings.add("최대 " + limit + "장 초과로 제외: " + original);
                log.info("[BoardImage] SKIP(OVER_MAX) postId={}, file={} (limit={})", postId, original, limit);
                continue;
            }

            long size = f.getSize();
            if (size > MAX_SIZE) {
                warnings.add("5MB 초과로 제외: " + original + " (" + humanMB(size) + ")");
                log.info("[BoardImage] SKIP(OVER_SIZE) postId={}, file={}, size={}", postId, original, size);
                continue;
            }

            String ct = safeLower(f.getContentType());
            String extFromType = (ct == null) ? null : CONTENT_TYPE_TO_EXT.get(ct);
            String extFromName = safeExtFromFilename(original);

            String ext;
            if (extFromType != null) {
                ext = extFromType;
            } else if (extFromName != null) {
                ext = extFromName;
            } else {
                warnings.add("타입 미지원으로 제외: " + original + (ct == null ? "" : " (" + ct + ")"));
                log.info("[BoardImage] SKIP(UNSUPPORTED_TYPE) postId={}, file={}, contentType={}", postId, original, ct);
                continue;
            }

            String stored = UUID.randomUUID() + ext;
            Path target = dir.resolve(stored).normalize();
            if (!target.startsWith(dir)) {
                warnings.add("저장 경로 오류로 제외: " + original);
                log.warn("[BoardImage] SKIP(INVALID_PATH) postId={}, file={}, target={}", postId, original, target);
                continue;
            }

            try (InputStream in = f.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                warnings.add("저장 실패로 제외: " + original);
                log.error("[BoardImage] SAVE_FAIL postId={}, file={}", postId, original, e);
                continue;
            }

            try {
                BoardPostImage img = new BoardPostImage();
                img.setPostId(postId);
                img.setOriginalName(trim255(original));
                img.setStoredName(stored);
                img.setContentType(ct);
                img.setSize(size);
                img.setUrl("/uploads/board/" + stored);
                img.setDisplayOrder(currentMaxOrder + saved + 1);
                repo.save(img);
                saved++;
            } catch (Exception e) {
                safeDeleteFile(target);
                warnings.add("DB 저장 실패로 제외: " + original);
                log.error("[BoardImage] DB_SAVE_FAIL postId={}, file={}", postId, original, e);
            }
        }

        return new ImageSaveResult(attempted, saved, warnings);
    }

    @Override
    public List<BoardPostImage> listByPostId(Long postId) {
        if (postId == null) return List.of();
        return repo.findByPostIdOrderByDisplayOrderAscIdAsc(postId);
    }

    @Override
    public long countByPostId(Long postId) {
        if (postId == null) return 0;
        return repo.countByPostId(postId);
    }

    @Override
    public ImageDeleteResult deleteImages(Long postId, List<Long> imageIds) {
        if (postId == null) return new ImageDeleteResult(0, 0, List.of("postId가 없어 삭제를 건너뜀"));
        if (imageIds == null || imageIds.isEmpty()) return new ImageDeleteResult(0, 0, List.of());

        List<BoardPostImage> targets = repo.findByPostIdAndIdIn(postId, imageIds);
        int requested = imageIds.size();
        int deleted = 0;
        List<String> warnings = new ArrayList<>();

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();

        for (BoardPostImage img : targets) {
            try {
                repo.delete(img);
                deleted++;
            } catch (Exception e) {
                warnings.add("DB 삭제 실패: " + safe(img.getOriginalName()));
                log.error("[BoardImage] DB_DELETE_FAIL postId={}, imageId={}", postId, img.getId(), e);
                continue;
            }

            try {
                if (img.getStoredName() != null && !img.getStoredName().isBlank()) {
                    Path file = dir.resolve(img.getStoredName()).normalize();
                    if (file.startsWith(dir)) {
                        Files.deleteIfExists(file);
                    }
                }
            } catch (Exception e) {
                warnings.add("파일 삭제 실패: " + safe(img.getOriginalName()));
                log.warn("[BoardImage] FILE_DELETE_FAIL postId={}, imageId={}", postId, img.getId(), e);
            }
        }

        if (deleted == 0 && requested > 0) {
            warnings.add("삭제할 이미지가 없었습니다. (이미 삭제되었거나 잘못된 선택일 수 있어요)");
        }

        return new ImageDeleteResult(requested, deleted, warnings);
    }

    @Override
    public void reorderImages(Long postId, List<Long> orderedImageIds) {
        if (postId == null) return;

        List<BoardPostImage> all = repo.findByPostIdOrderByDisplayOrderAscIdAsc(postId);
        if (all.isEmpty()) return;

        Map<Long, BoardPostImage> byId = new LinkedHashMap<>();
        for (BoardPostImage img : all) {
            if (img.getId() != null) byId.put(img.getId(), img);
        }

        List<Long> safeOrdered = (orderedImageIds == null) ? List.of() : orderedImageIds;

        int order = 1;

        for (Long id : safeOrdered) {
            if (id == null) continue;
            BoardPostImage img = byId.remove(id);
            if (img == null) continue;
            img.setDisplayOrder(order++);
        }

        for (BoardPostImage img : byId.values()) {
            img.setDisplayOrder(order++);
        }

        try {
            repo.saveAll(all);
            log.info("[BoardImage] REORDER postId={}, orderedCount={}, total={}",
                    postId, safeOrdered.size(), all.size());
        } catch (Exception e) {
            log.error("[BoardImage] REORDER_FAIL postId={}", postId, e);
        }
    }

    private String safeExtFromFilename(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return null;

        String ext = filename.substring(dot).toLowerCase(Locale.ROOT).trim();

        if (ext.equals(".jpg") || ext.equals(".jpeg")) return ".jpg";
        if (ext.equals(".png")) return ".png";
        if (ext.equals(".webp")) return ".webp";
        if (ext.equals(".gif")) return ".gif";
        if (ext.equals(".avif")) return ".avif";

        return null;
    }

    private String trim255(String s) {
        if (s == null) return "";
        return s.length() > 255 ? s.substring(0, 255) : s;
    }

    private String safeLower(String s) {
        if (s == null) return null;
        return s.toLowerCase(Locale.ROOT).trim();
    }

    private String humanMB(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format(Locale.KOREA, "%.2fMB", mb);
    }

    private void safeDeleteFile(Path p) {
        try { Files.deleteIfExists(p); } catch (Exception ignore) {}
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "(이름없음)" : s;
    }
}
