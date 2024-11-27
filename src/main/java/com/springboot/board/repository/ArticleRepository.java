package com.springboot.board.repository;

import com.springboot.board.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
