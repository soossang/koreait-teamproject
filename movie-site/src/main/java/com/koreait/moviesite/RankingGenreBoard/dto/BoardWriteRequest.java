package com.koreait.moviesite.RankingGenreBoard.dto;

public class BoardWriteRequest {
    private String title;
    private String content;

    public BoardWriteRequest() {}

    public BoardWriteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
