package com.koreait.moviesite.RankingGenreBoard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "board_post_images",
        indexes = {
                @Index(name = "idx_board_post_images_post_id", columnList = "post_id")
        }
)
public class BoardPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 ID (BoardPost 엔티티를 ManyToOne으로 묶지 않고 단순 FK처럼 사용)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 예: /uploads/board/uuid.jpg
    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column
    private Long size;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ===== getter/setter =====
    public Long getId() { return id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
