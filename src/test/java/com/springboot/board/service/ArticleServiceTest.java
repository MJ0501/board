package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.domain.constant.SearchType;
import com.springboot.board.dto.ArticleDto;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스로직 - Article")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks private ArticleService sut;
    @Mock private ArticleRepository articleRepository;

    /////* Search */
    @DisplayName("SearchKeyword X -> ArticlePage")
    @Test
    void givenNoSearchKeyword_whenSearchingArticles_thenReturnsArticlePage() {
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());
        // When
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);
        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("SearchKeyword -> ArticlePage")
    @Test
    void givenSearchKeyword_whenSuccessSearching_thenReturnArticlePage(){
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());
        //when
        Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);
        //then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
    }

    @DisplayName("article 단건조회")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
        // When
        ArticleWithCommentsDto dto = sut.getArticle(articleId);
        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }
    // 게시글이 없는 경우
    @DisplayName("없는 게시글 조회 -> ThrowsException")
    @Test
    void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());
        // When
        Throwable t = catchThrowable(() -> sut.getArticle(articleId));
        // Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("SearchKeyword X & SearchingHashTag -> EmptyPage ")
    @Test
    void givenNoSearchKeyword_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        Pageable pageable = Pageable.ofSize(20);
        // When
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(null, pageable);
        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("Hashtag -> ArticlesPage")
    @Test
    void givenHashtag_whenSuccessSearching_thenReturnsArticlesPage() {
        String hashtag = "#java";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtag(hashtag, pageable)).willReturn(Page.empty(pageable));
        // When
        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtag, pageable);
        // Then
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtag(hashtag, pageable);
    }


    /////* CREATE */
    @DisplayName("ArticleInfo -> Create Article")
    @Test
    void givenArticleInfo_whenSuccessSaving_thenSavesArticle(){
        ArticleDto dto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());
        //when
        sut.saveArticle(dto);
        //then
        then(articleRepository).should().save(any(Article.class));
    }

    /////* UPDATE */
    @DisplayName("ArticleInfo -> Update Article")
    @Test
    void givenArticleInfo_whenSavingArticle_thenUpdatesArticle(){
        Article article = createArticle();
        ArticleDto dto = createArticleDto();
        //when
        sut.updateArticle(dto);
        //then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", dto.title())
                .hasFieldOrPropertyWithValue("content",dto.content())
                .hasFieldOrPropertyWithValue("hashtag",dto.hashtag());
        then(articleRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("존재하지 않는 Article 수정시, ThrowException")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);
        // When
        sut.updateArticle(dto);
        // Then
        then(articleRepository).should().getReferenceById(dto.id());
    }

    /////* DELETE */
    @DisplayName("ArticleID -> Delete Article")
    @Test
    void givenArticleId_thenDeletesArticle(){
        Long articleId = 1L;
        willDoNothing().given(articleRepository).deleteById(articleId);
        //when
        sut.deleteArticle(1L);
        //then
        then(articleRepository).should().deleteById(articleId);
    }

    private Article createArticle() {
        return Article.of(createUserAccount(), "title","content","#java");
    }

    private UserAccount createUserAccount() {
        return UserAccount.of("MJ","pw","MJ@mail.com","MJ",null);
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title","content","#java");
    }
    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(1L,createUserAccountDto(),title,content,hashtag, LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }
    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ","pw","MJ@mail.com","MJ","mmeemmoo",LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }

}