package com.springboot.board.repository.querydsl;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.QArticle;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import java.util.List;

public class ArticleRepositoryCustomImpl extends QuerydslRepositorySupport implements ArticleRepositoryCustom {

    public ArticleRepositoryCustomImpl() {
        super(Article.class);
    }

    @Override
    public List<String> findAllDistinctHashtags() {
        QArticle article = QArticle.article;
        /*
        * JPQLQuery<String> query = from(article).distinct().select(article.hashtag) JPQLQuery<String>.where(article.hashtag.isNotNull());
        * return query.fetch();
        * */
        return from(article).distinct().select(article.hashtag)
                .where(article.hashtag.isNotNull()).fetch();
    }
}
