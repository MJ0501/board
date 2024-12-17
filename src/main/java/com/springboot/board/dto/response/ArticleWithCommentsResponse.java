package com.springboot.board.dto.response;
import com.springboot.board.dto.ArticleWithCommentsDto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleWithCommentsResponse(
        Long id, String email, String nickname, String userId,
        String title, String content, String hashtag, LocalDateTime createdAt,
        Set<ArticleCommentsResponse> articleCommentsResponse) {

    public static ArticleWithCommentsResponse of(Long id, String email, String nickname, String userId, String title, String content, String hashtag, LocalDateTime createdAt, Set<ArticleCommentsResponse> articleCommentsResponses) {
        return new ArticleWithCommentsResponse(id, email, nickname, userId, title, content, hashtag, createdAt, articleCommentsResponses);
    }

    public static ArticleWithCommentsResponse from(ArticleWithCommentsDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isEmpty()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleWithCommentsResponse(
                dto.id(),dto.userAccountDto().email(), nickname, dto.userAccountDto().userId(),
                dto.title(), dto.content(), dto.hashtag(), dto.createdAt(),
                dto.articleCommentDtos().stream().map(ArticleCommentsResponse::from).collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
