package com.springboot.board.dto.response;
import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.dto.HashtagDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ArticleWithCommentsResponse(
        Long id, String email, String nickname, String userId,
        String title, String content,  Set<String> hashtags, LocalDateTime createdAt,
        Set<ArticleCommentResponse> articleCommentResponse
) {
    public static ArticleWithCommentsResponse of(Long id, String email, String nickname, String userId, String title, String content,  Set<String> hashtags, LocalDateTime createdAt, Set<ArticleCommentResponse> articleCommentResponse) {
        return new ArticleWithCommentsResponse(id, email, nickname, userId, title, content, hashtags, createdAt, articleCommentResponse);
    }

    public static ArticleWithCommentsResponse from(ArticleWithCommentsDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isEmpty()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleWithCommentsResponse(
                dto.id(),dto.userAccountDto().email(), nickname, dto.userAccountDto().userId(),
                dto.title(), dto.content(), dto.hashtagDtos().stream().map(HashtagDto::hashtagName).collect(Collectors.toUnmodifiableSet()), dto.createdAt(),
                organizeChildComments(dto.articleCommentDtos()));
    }

    private static Set<ArticleCommentResponse> organizeChildComments(Set<ArticleCommentDto> dtos) {
        Map<Long, ArticleCommentResponse> map = dtos.stream()
                .map(ArticleCommentResponse::from)
                .collect(Collectors.toMap(ArticleCommentResponse::id, Function.identity()));

        map.values().stream().filter(ArticleCommentResponse::hasParentComment)
                .forEach(comment -> {
                    ArticleCommentResponse parentComment = map.get(comment.parentCommentId());
                    parentComment.childComments().add(comment);});

        return map.values().stream().filter(comment -> !comment.hasParentComment())
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator
                        .comparing(ArticleCommentResponse::createdAt)
                        .reversed()
                        .thenComparingLong(ArticleCommentResponse::id))));
    }
}
