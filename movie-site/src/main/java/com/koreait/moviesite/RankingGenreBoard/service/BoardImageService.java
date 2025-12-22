package com.koreait.moviesite.RankingGenreBoard.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.koreait.moviesite.RankingGenreBoard.entity.BoardPostImage;

public interface BoardImageService {

    /**
     * 게시글(postId)에 첨부된 이미지들을 저장한다.
     * - images는 null/empty일 수 있음(그 경우 아무 것도 하지 않아도 됨)
     */
    void saveImages(Long postId, List<MultipartFile> images);

    /**
     * 게시글(postId)에 연결된 이미지 목록을 조회한다.
     */
    List<BoardPostImage> listByPostId(Long postId);
}
