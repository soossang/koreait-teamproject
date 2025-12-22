package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;
import com.koreait.moviesite.RankingGenreBoard.repository.BoardPostImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

@Service
public class BoardImageServiceImpl implements BoardImageService {

    private static final int MAX_COUNT = 5;
    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final BoardPostImageRepository repo;

    // 기본: 프로젝트 실행 폴더 기준 uploads/board
    @Value("${app.upload.board-dir:uploads/board}")
    private String uploadDir;

    public BoardImageServiceImpl(BoardPostImageRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveImages(Long postId, List<MultipartFile> images) {
        if (postId == null) return;
        if (images == null || images.isEmpty()) return;

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new RuntimeException("업로드 폴더 생성 실패: " + dir, e);
        }

        int saved = 0;

        for (MultipartFile f : images) {
            if (f == null || f.isEmpty()) continue;
            if (saved >= MAX_COUNT) break;

            // 용량 제한
            if (f.getSize() > MAX_SIZE) {
                continue;
            }

            // 타입 제한
            String ct = f.getContentType();
            if (ct == null || !ct.startsWith("image/")) {
                continue;
            }
            String extFromType = CONTENT_TYPE_TO_EXT.get(ct);
            if (extFromType == null) {
                // image/svg+xml 같은 애매한 타입은 스킵(정적 서빙/보안 이슈 줄이기)
                continue;
            }

            // 원본명 정리
            String original = StringUtils.cleanPath(Objects.toString(f.getOriginalFilename(), ""));
            if (original.isBlank()) original = "image" + extFromType;

            // 확장자 추출(없거나 이상하면 contentType 기반 확장자 사용)
            String ext = safeExtFromFilename(original);
            if (ext == null) ext = extFromType;

            // 저장 파일명
            String stored = UUID.randomUUID() + ext;
            Path target = dir.resolve(stored).normalize();

            // 혹시라도 이상한 경로가 되지 않도록 안전장치
            if (!target.startsWith(dir)) {
                throw new RuntimeException("잘못된 저장 경로 감지: " + target);
            }

            // 파일 저장
            try (InputStream in = f.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("이미지 파일 저장 실패: " + original, e);
            }

            // DB 저장
            BoardPostImage img = new BoardPostImage();
            img.setPostId(postId);
            img.setOriginalName(trim255(original));
            img.setStoredName(stored);
            img.setContentType(ct);
            img.setSize(f.getSize());

            // WebConfig의 /uploads/board/** 매핑과 동일해야 함
            img.setUrl("/uploads/board/" + stored);

            repo.save(img);
            saved++;
        }
    }

    @Override
    public List<BoardPostImage> listByPostId(Long postId) {
        if (postId == null) return List.of();
        return repo.findByPostIdOrderByIdAsc(postId);
    }

    // -----------------------
    // helpers
    // -----------------------
    private String safeExtFromFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return null;

        String ext = filename.substring(dot).toLowerCase(Locale.ROOT).trim();
        // 허용 확장자만 인정
        if (ext.equals(".jpg") || ext.equals(".jpeg")) return ".jpg";
        if (ext.equals(".png")) return ".png";
        if (ext.equals(".webp")) return ".webp";
        if (ext.equals(".gif")) return ".gif";

        return null;
    }

    private String trim255(String s) {
        if (s == null) return "";
        return s.length() > 255 ? s.substring(0, 255) : s;
    }
}
