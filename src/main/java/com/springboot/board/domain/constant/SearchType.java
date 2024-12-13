package com.springboot.board.domain.constant;

import lombok.Getter;

@Getter
public enum SearchType {
    TITLE("제목"),
    CONTENT("내용"),
    ID("유저ID"),
    NICKNAME("닉네임"),
    HASHTAG("해시태그");

    private final String description;

    SearchType(String description) {
        this.description = description;
    }
}
