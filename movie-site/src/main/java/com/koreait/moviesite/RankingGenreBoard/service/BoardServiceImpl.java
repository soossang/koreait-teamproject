package com.koreait.moviesite.RankingGenreBoard.service;

import com.koreait.moviesite.RankingGenreBoard.dao.BoardCommentRepository;
import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardPost;
import com.koreait.moviesite.RankingGenreBoard.repository.BoardPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardPostRepository postRepo;
    private final BoardCommentRepository commentRepo;

    public BoardServiceImpl(BoardPostRepository postRepo, BoardCommentRepository commentRepo) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
    }

    @Override
    public Page<BoardDtos.PostResponse> list(Pageable pageable) {
        return postRepo.findAll(pageable).map(this::toPostResponse);
    }

    @Override
    @Transactional
    public BoardDtos.PostResponse get(Long id, boolean increaseView) {
        BoardPost post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("post not found: " + id));

        if (increaseView) {
            post.setViewCount(post.getViewCount() + 1);
        }
        return toPostResponse(post);
    }

    @Override
    public Long create(BoardDtos.PostCreateRequest req) {
        BoardPost p = new BoardPost();
        // ✅ BoardDtos.*Request 는 record 이므로 accessor 메서드는 getXxx()가 아니라 xxx() 입니다.
        p.setTitle(req.title());
        p.setContent(req.content());
        p.setAuthor(req.author());
        return postRepo.save(p).getId();
    }

    @Override
    public void update(Long id, BoardDtos.PostUpdateRequest req) {
        BoardPost p = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("post not found: " + id));

        p.setTitle(req.title());
        p.setContent(req.content());
        postRepo.save(p);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // FK(board_comment.post_id) 때문에 댓글 먼저 삭제
        commentRepo.deleteByPost_Id(id);
        postRepo.deleteById(id);
    }

    @Override
    @Transactional
    public Long addComment(Long postId, BoardDtos.CommentCreateRequest req) {
        BoardPost p = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post not found: " + postId));

        BoardComment c = new BoardComment();
        c.setPost(p);
        c.setAuthor(req.author());
        c.setContent(req.content());
        return commentRepo.save(c).getId();
    }

    private BoardDtos.PostResponse toPostResponse(BoardPost p) {
        List<BoardDtos.CommentResponse> comments = (p.getId() == null)
                ? List.of()
                : commentRepo.findByPost_IdOrderByIdAsc(p.getId()).stream()
                .map(c -> new BoardDtos.CommentResponse(
                        c.getId(),
                        c.getAuthor(),
                        c.getContent(),
                        c.getCreatedAt()
                ))
                .toList();

        return new BoardDtos.PostResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getAuthor(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getViewCount(),
                comments
        );
    }
    
    @Override
    @Transactional
    public void updateComment(Long postId, Long commentId, String loginId, String content) {
        BoardComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentId));

        if (!c.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("post mismatch");
        }
        if (!c.getAuthor().equals(loginId)) {
            throw new IllegalStateException("not author");
        }

        c.setContent(content);
        commentRepo.save(c);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String loginId) {
        BoardComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment not found: " + commentId));

        if (!c.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("post mismatch");
        }
        if (!c.getAuthor().equals(loginId)) {
            throw new IllegalStateException("not author");
        }

        commentRepo.delete(c);
    }

}
