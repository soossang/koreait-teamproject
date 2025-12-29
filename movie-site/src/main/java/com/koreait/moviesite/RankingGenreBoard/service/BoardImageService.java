package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardImageService {

    int MAX_COUNT = 5;
    long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    record ImageSaveResult(int attempted, int saved, List<String> warnings) {}
    record ImageDeleteResult(int requested, int deleted, List<String> warnings) {}

    ImageSaveResult saveImages(Long postId, List<MultipartFile> images, int maxToSave);

    default ImageSaveResult saveImages(Long postId, List<MultipartFile> images) {
        return saveImages(postId, images, MAX_COUNT);
    }

    List<BoardPostImage> listByPostId(Long postId);

    long countByPostId(Long postId);

    ImageDeleteResult deleteImages(Long postId, List<Long> imageIds);

    /**
     * ✅ 드래그로 이미지 순서 변경
     * orderedImageIds: 화면에서 정해진 "최종 순서"(1..n)
     * - postId에 속한 이미지들만 대상으로 적용
     * - orderedImageIds에 없는(누락된) 이미지가 있으면 맨 뒤로 붙인다.
     */
    void reorderImages(Long postId, List<Long> orderedImageIds);
}
