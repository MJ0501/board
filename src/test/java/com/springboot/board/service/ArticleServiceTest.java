package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.domain.constant.SearchType;
import com.springboot.board.dto.ArticleDto;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.ArticleRepository;
import com.springboot.board.repository.UserAccountRepository;
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
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import org.springframework.test.util.ReflectionTestUtils;


@DisplayName("비즈니스로직 - Article")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    private ArticleService sut;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    /////* CREATE */
    @DisplayName("2. ArticleInfo -> Create Article")
    @Test
    void givenArticleInfo_whenSuccessSaving_thenSavesArticle(){
        ArticleDto dto = createArticleDto();
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        sut.saveArticle(dto);
        then(articleRepository).should().save(any(Article.class));
    }

    /////* READ */
    @DisplayName("1. 없는 게시글 조회 -> ThrowsException")
    @Test
    void givenNonexistentArticleId_whenSearchingArticle_thenThrowsException() {
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());
        Throwable t = catchThrowable(() -> sut.getArticle(articleId));

        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다. articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("1.2 article detail + with Comments")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticleWithComments() {
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        ArticleWithCommentsDto dto = sut.getArticleWithComments(articleId);
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("1.2 댓글 달린 게시글이 없으면, 예외를 던진다.")
    @Test
    void givenNonexistentArticleId_whenSearchingArticleWithComments_thenThrowsException() {
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        Throwable t = catchThrowable(() -> sut.getArticleWithComments(articleId));
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 게시글이 없습니다. articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    /////* UPDATE */
    @DisplayName("ArticleInfo -> Update Article")
    @Test
    void givenArticleInfo_whenSavingArticle_thenUpdatesArticle(){
        Article article = createArticle();
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willReturn(article);

        sut.updateArticle(dto.id(), dto);
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

        sut.updateArticle(dto.id(), dto);
        then(articleRepository).should().getReferenceById(dto.id());
    }

    /////* DELETE */
    @DisplayName("ArticleID -> Delete Article")
    @Test
    void givenArticleId_thenDeletesArticle(){
        Long articleId = 1L;
        willDoNothing().given(articleRepository).deleteById(articleId);

        sut.deleteArticle(1L);
        then(articleRepository).should().deleteById(articleId);
    }

    /* Search & hashtag Search*/
    @DisplayName("SearchKeyword X -> ArticlePage")
    @Test
    void givenNoSearchKeyword_whenSearchingArticles_thenReturnsArticlePage() {
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);

        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("SearchKeyword O -> ArticlePage")
    @Test
    void givenSearchKeyword_whenSuccessSearching_thenReturnArticlePage(){
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

        Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable);
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitleContaining(searchKeyword, pageable);
    }

    @DisplayName("SearchKeyword X & SearchingHashTag -> EmptyPage")
    @Test
    void givenNoSearchKeyword_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        Pageable pageable = Pageable.ofSize(20);

        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(null, pageable);
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("Searching hashtag -> ArticlesPage")
    @Test
    void givenHashtag_whenSuccessSearching_thenReturnsArticlesPage() {
        String hashtag = "#java";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtag(hashtag, pageable)).willReturn(Page.empty(pageable));

        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtag, pageable);
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtag(hashtag, pageable);
    }

    @DisplayName("Hashtag List -> 존재하는 HashTags")
    @Test
    void givenNothing_whenCalling_thenReturnsHashtags() {
        List<String> expectedHashtags = List.of("#java", "#spring", "#boot");
        given(articleRepository.findAllDistinctHashtags()).willReturn(expectedHashtags);

        List<String> actualHashtags = sut.getHashtags();
        assertThat(actualHashtags).isEqualTo(expectedHashtags);
        then(articleRepository).should().findAllDistinctHashtags();
    }


    private Article createArticle() {
        Article article= Article.of(createUserAccount(), "title","content","#java");
        ReflectionTestUtils.setField(article, "id",1L);
        return article;
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
        return UserAccountDto.of("MJ","m0501","MJ@mail.com","MJ","mmeemmoo",LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }

}