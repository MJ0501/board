package com.springboot.board.domain;

import java.time.LocalDateTime;

public class ArticleComment {
    private Long id;
    private Article article;    // 게시글ID
    private String content;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime modifiedAt;
    private String modifiedBy;
}
