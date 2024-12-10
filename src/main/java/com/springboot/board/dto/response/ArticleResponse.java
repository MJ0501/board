package com.springboot.board.dto.response;

import com.springboot.board.domain.Article;
import com.springboot.board.dto.ArticleDto;

import java.time.LocalDateTime;

public record ArticleResponse(
        Long id, String email, String nickname, String title, String content,
        String hashtag, LocalDateTime createdAt) {

    public static ArticleResponse of(Long id, String email, String nickname, String title, String content, String hashtag, LocalDateTime createdAt) {
        return new ArticleResponse(id, email, nickname, title, content, hashtag, createdAt);
    }

    public static ArticleResponse from(ArticleDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleResponse(dto.id(),dto.userAccountDto().email(), nickname, dto.title(), dto.content(), dto.hashtag(), dto.createdAt());
    }
}
