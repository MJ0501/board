package com.springboot.board.repository;

import com.springboot.board.domain.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
}
