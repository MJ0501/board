package com.springboot.board.dto.response;
import com.springboot.board.domain.Article;
import com.springboot.board.dto.ArticleWithCommentsDto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleWithCommentResponse(
        Long id, String email, String nickname,
        String title, String content, String hashtag, LocalDateTime createdAt,
        Set<ArticleCommentResponse> articleCommentResponses) {

    public static ArticleWithCommentResponse of(Long id, String email, String nickname, String title, String content, String hashtag, LocalDateTime createdAt, Set<ArticleCommentResponse> articleCommentResponses) {
        return new ArticleWithCommentResponse(id, email, nickname, title, content, hashtag, createdAt, articleCommentResponses);
    }

    public static ArticleWithCommentResponse from(ArticleWithCommentsDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isEmpty()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleWithCommentResponse(
                dto.id(),dto.userAccountDto().email(), nickname,
                dto.title(), dto.content(), dto.hashtag(), dto.createdAt(),
                dto.articleCommentDtos().stream().map(ArticleCommentResponse::from).collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }
}
