package com.koreait.moviesite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_comment")
public class BoardComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private BoardPost post;

    @Column(nullable=false, length=50)
    private String author;

    @Lob @Column(nullable=false)
    private String content;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { createdAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public BoardPost getPost() { return post; } public void setPost(BoardPost post) { this.post = post; }
    public String getAuthor() { return author; } public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
