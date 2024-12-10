package com.springboot.board.dto.response;

import com.springboot.board.domain.ArticleComment;
import com.springboot.board.dto.ArticleCommentDto;

import java.time.LocalDateTime;

public record ArticleCommentResponse(
        Long id, String email, String nickname,
        String content, LocalDateTime createdAt) {

    public static ArticleCommentResponse of(Long id, String email, String nickname, String content, LocalDateTime createdAt) {
        return new ArticleCommentResponse(id, email, nickname, content, createdAt);
    }

    public static ArticleCommentResponse from(ArticleCommentDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isEmpty()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleCommentResponse(dto.id(), dto.userAccountDto().email(), nickname, dto.content(), dto.createdAt());
    }
}
