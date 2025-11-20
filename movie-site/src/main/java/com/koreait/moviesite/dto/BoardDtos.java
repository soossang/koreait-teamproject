package com.koreait.moviesite.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public class BoardDtos {
    public record PostResponse(Long id, String title, String content, String author,
                               LocalDateTime createdAt, LocalDateTime updatedAt, long viewCount,
                               List<CommentResponse> comments) {}

    public record PostCreateRequest(
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String author
    ) {}

    public record PostUpdateRequest(
            @NotBlank String title,
            @NotBlank String content
    ) {}

    public record CommentCreateRequest(
            @NotBlank String author,
            @NotBlank String content
    ) {}

    public record CommentResponse(Long id, String author, String content, LocalDateTime createdAt) {}
}
