package com.springboot.board.repository.querydsl;

import java.util.List;
//commit추가설명확인
public interface ArticleRepositoryCustom {
    List<String> findAllDistinctHashtags();
}
