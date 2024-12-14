package com.springboot.board.repository;

import com.springboot.board.config.JpaConfig;
import com.springboot.board.domain.Article;
import com.springboot.board.domain.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {
    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;

    JpaRepositoryTest(
            @Autowired ArticleRepository articleRepository,
            @Autowired ArticleCommentRepository articleCommentRepository,
            @Autowired UserAccountRepository userAccountRepository){
        this.articleRepository = articleRepository;
        this.articleCommentRepository = articleCommentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @DisplayName("select TEST")
    void givenTestData_whenSelecting_thenWorksFine() {
        List<Article> articles = articleRepository.findAll();

        assertThat(articles)
                .isNotNull()
                .hasSize(123);
    }

    @DisplayName("insert TEST")
    @Test
    void givenTestData_whenInserting_thenWorksFine() {
        long previousCount = articleRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("MJ","pw", null, null, null));
        Article article = Article.of(userAccount, "new article", "new content", "#spring");
        articleRepository.save(article);

        assertThat(articleRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update TEST")
    @Test
    void givenTestData_whenUpdating_thenWorksFine() {
        Article article = articleRepository.findById(1L).orElseThrow();
        String updatedHashtag = "#springboot";
        article.setHashtag(updatedHashtag);

        Article savedArticle = articleRepository.saveAndFlush(article);
        assertThat(savedArticle).hasFieldOrPropertyWithValue("hashtag", updatedHashtag);
    }

    @DisplayName("delete TEST")
    @Test
    void givenTestData_whenDeleting_thenWorksFine() {
        Article article = articleRepository.findById(1L).orElseThrow();
        long previousArticleCount = articleRepository.count();
        long previousArticleCommentCount = articleCommentRepository.count();
        int deletedCommentsSize = article.getArticleComments().size();

        articleRepository.delete(article);
        assertThat(articleRepository.count()).isEqualTo(previousArticleCount - 1);
        assertThat(articleCommentRepository.count()).isEqualTo(previousArticleCommentCount - deletedCommentsSize);
    }

}
