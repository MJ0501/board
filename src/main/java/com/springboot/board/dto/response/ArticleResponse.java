package com.springboot.board.dto.response;

import com.springboot.board.dto.ArticleDto;
import com.springboot.board.dto.HashtagDto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ArticleResponse(
        Long id, String email, String nickname, String title, String content,
        Set<String> hashtags, LocalDateTime createdAt
) {
    public static ArticleResponse of(Long id, String email, String nickname, String title, String content, Set<String> hashtags, LocalDateTime createdAt) {
        return new ArticleResponse(id, email, nickname, title, content,hashtags, createdAt);
    }

    public static ArticleResponse from(ArticleDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if(nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().userId();
        }
        return new ArticleResponse(dto.id(),dto.userAccountDto().email(), nickname, dto.title(), dto.content(),
                dto.hashtagDtos().stream().map(HashtagDto::hashtagName).collect(Collectors.toUnmodifiableSet()), dto.createdAt());
    }
}
