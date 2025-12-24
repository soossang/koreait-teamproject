package com.koreait.moviesite.Common.controller;

import com.koreait.moviesite.Member.entity.MemberRole;
import com.koreait.moviesite.Member.security.AuthenticatedMember;
import com.koreait.moviesite.RankingGenreBoard.dao.BoardCommentRepository;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardPost;
import com.koreait.moviesite.RankingGenreBoard.repository.BoardPostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/board")
public class AdminBoardApiController {

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;

    public AdminBoardApiController(BoardPostRepository boardPostRepository,
                                  BoardCommentRepository boardCommentRepository) {
        this.boardPostRepository = boardPostRepository;
        this.boardCommentRepository = boardCommentRepository;
    }

    private boolean isAdmin(AuthenticatedMember auth) {
        return auth != null && auth.role() == MemberRole.ADMIN;
    }

    public record BoardPostAdminRow(
            Long id,
            String title,
            String author,
            long viewCount,
            long commentCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    @GetMapping("/posts")
    public ResponseEntity<?> listPosts(@RequestAttribute("authMember") AuthenticatedMember authMember) {
        if (!isAdmin(authMember)) {
            return ResponseEntity.status(403).build();
        }

        List<BoardPost> posts = boardPostRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<BoardPostAdminRow> rows = posts.stream()
                .map(p -> new BoardPostAdminRow(
                        p.getId(),
                        p.getTitle(),
                        p.getAuthor(),
                        p.getViewCount(),
                        boardCommentRepository.countByPost_Id(p.getId()),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(rows);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(
            @RequestAttribute("authMember") AuthenticatedMember authMember,
            @PathVariable("id") Long id
    ) {
        if (!isAdmin(authMember)) {
            return ResponseEntity.status(403).build();
        }
        if (!boardPostRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("message", "게시글을 찾을 수 없습니다."));
        }

        // FK 제약 때문에 댓글 먼저 삭제
        boardCommentRepository.deleteByPost_Id(id);
        boardPostRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }
}
