package com.koreait.moviesite.RankingGenreBoard.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.koreait.moviesite.RankingGenreBoard.dao.BoardCommentRepository;
import com.koreait.moviesite.RankingGenreBoard.dao.BoardPostRepository;
import com.koreait.moviesite.RankingGenreBoard.dto.BoardDtos;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardComment;
import com.koreait.moviesite.RankingGenreBoard.entity.BoardPost;

import java.util.List;

@Service
public class BoardServiceImpl implements BoardService {
    private final BoardPostRepository postRepo;
    private final BoardCommentRepository commentRepo;
    public BoardServiceImpl(BoardPostRepository p, BoardCommentRepository c){ this.postRepo=p; this.commentRepo=c; }

    @Override
    public Page<BoardDtos.PostResponse> list(Pageable pageable) {
        return postRepo.findAll(pageable).map(this::toPostResponse);
    }

    @Override
    @Transactional
    public BoardDtos.PostResponse get(Long id, boolean increaseView) {
        BoardPost post = postRepo.findById(id).orElseThrow();
        if (increaseView) { post.setViewCount(post.getViewCount()+1); }
        return toPostResponse(post);
    }

    @Override
    public Long create(BoardDtos.PostCreateRequest req) {
        BoardPost p = new BoardPost();
        p.setTitle(req.title());
        p.setContent(req.content());
        p.setAuthor(req.author());
        return postRepo.save(p).getId();
    }

    @Override
    public void update(Long id, BoardDtos.PostUpdateRequest req) {
        BoardPost p = postRepo.findById(id).orElseThrow();
        p.setTitle(req.title());
        p.setContent(req.content());
        postRepo.save(p);
    }

    @Override
    public void delete(Long id) { postRepo.deleteById(id); }

    @Override
    public Long addComment(Long postId, BoardDtos.CommentCreateRequest req) {
        BoardPost p = postRepo.findById(postId).orElseThrow();
        BoardComment c = new BoardComment();
        c.setPost(p);
        c.setAuthor(req.author());
        c.setContent(req.content());
        return commentRepo.save(c).getId();
    }

    private BoardDtos.PostResponse toPostResponse(BoardPost p) {
        List<BoardDtos.CommentResponse> comments = p.getId()==null ? List.of() :
                commentRepo.findAll().stream()     // 단순 틀: 실제로는 postId 기준 쿼리로 바꾸면 더 효율적
                        .filter(c -> c.getPost().getId().equals(p.getId()))
                        .map(c -> new BoardDtos.CommentResponse(c.getId(), c.getAuthor(), c.getContent(), c.getCreatedAt()))
                        .toList();
        return new BoardDtos.PostResponse(p.getId(), p.getTitle(), p.getContent(), p.getAuthor(),
                p.getCreatedAt(), p.getUpdatedAt(), p.getViewCount(), comments);
    }
}
