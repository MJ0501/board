package com.springboot.board.repository.querydsl;

import java.util.List;

public interface HashtagRepositoryCustom {
    List<String> findAllHashtagNames();
}
