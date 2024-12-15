package com.springboot.board.dto.request;

import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.dto.UserAccountDto;

public record ArticleCommentRequest(Long articleId, String content) {

    public static ArticleCommentRequest of(Long articleId, String content) {
        return new ArticleCommentRequest(articleId, content);
    }

    public ArticleCommentDto toDto(UserAccountDto userAccountDto){
        return ArticleCommentDto.of(articleId,userAccountDto,content);
    }
}
