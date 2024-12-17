package com.springboot.board.dto.response;

import com.springboot.board.dto.ArticleCommentDto;
import java.time.LocalDateTime;

public record ArticleCommentsResponse(
        Long id, String email, String nickname, String userId,
        String content, LocalDateTime createdAt) {

    public static ArticleCommentsResponse of(Long id, String email, String nickname, String userId,String content, LocalDateTime createdAt) {
        return new ArticleCommentsResponse(id, email, nickname, userId,content, createdAt);
    }

    public static ArticleCommentsResponse from(ArticleCommentDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isEmpty()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleCommentsResponse(dto.id(), dto.userAccountDto().email(), nickname, dto.userAccountDto().userId(),dto.content(), dto.createdAt());
    }
}
