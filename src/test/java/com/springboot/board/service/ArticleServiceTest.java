package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.Hashtag;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.domain.constant.SearchType;
import com.springboot.board.dto.ArticleDto;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.dto.HashtagDto;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.ArticleRepository;
import com.springboot.board.repository.HashtagRepository;
import com.springboot.board.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Mock
    private HashtagRepository hashtagRepository;
    @Mock
    private HashtagService hashtagService;

    /////* CREATE */
    @DisplayName("ArticleInfo -> Create Article with hashtagInfo(content에서 hashtagInfo추출)")
    @Test
    void givenArticleInfo_whenSuccessSaving_thenExtractsHashtagsFromContentAndSavesArticle(){
        ArticleDto dto = createArticleDto();
        Set<String> expectedHashtagNames = Set.of("java", "spring");
        Set<Hashtag> expectedHashtags = new HashSet<>();
        expectedHashtags.add(createHashtag("java"));

        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        given(hashtagService.parseHashtagNames(dto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        sut.saveArticle(dto);
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(hashtagService).should().parseHashtagNames(dto.content());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
        then(articleRepository).should().save(any(Article.class));
    }

    /////* READ */
    @DisplayName("없는 게시글 조회 -> ThrowsException")
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

    @DisplayName("article detail + with Comments")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticleWithComments() {
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        ArticleWithCommentsDto dto = sut.getArticleWithComments(articleId);
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtagDtos", article.getHashtags().stream().map(HashtagDto::from).collect(Collectors.toUnmodifiableSet()));
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("댓글 달린 게시글이 없으면, 예외를 던진다.")
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
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용 #springboot");
        Set<String> expectedHashtagNames = Set.of("springboot");
        Set<Hashtag> expectedHashtags = new HashSet<>();

        given(articleRepository.getReferenceById(dto.id())).willReturn(article);
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(dto.userAccountDto().toEntity());
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());
        given(hashtagService.parseHashtagNames(dto.content())).willReturn(expectedHashtagNames);
        given(hashtagService.findHashtagsByNames(expectedHashtagNames)).willReturn(expectedHashtags);

        sut.updateArticle(dto.id(), dto);
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", dto.title())
                .hasFieldOrPropertyWithValue("content",dto.content())
                .extracting("hashtags", as(InstanceOfAssertFactories.COLLECTION)).hasSize(1)
                        .extracting("hashtagName").containsExactly("springboot");

        then(articleRepository).should().getReferenceById(dto.id());
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
        then(hashtagService).should().parseHashtagNames(dto.content());
        then(hashtagService).should().findHashtagsByNames(expectedHashtagNames);
    }

    @DisplayName("존재하지 않는 Article 수정시, ThrowException")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용");
        given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

        sut.updateArticle(dto.id(), dto);
        then(articleRepository).should().getReferenceById(dto.id());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(hashtagService).shouldHaveNoInteractions();
    }

    @DisplayName("게시글 작성자가 아닌 사람이 UpdateArticle -> Nothing")
    @Test
    void givenModifiedArticleInfoWithDifferentUser_whenUpdatingArticle_thenDoesNothing() {
        Long differentArticleId = 22L;
        Article differentArticle = createArticle(differentArticleId);
        differentArticle.setUserAccount(createUserAccount("John"));
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용");
        given(articleRepository.getReferenceById(differentArticleId)).willReturn(differentArticle);
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(dto.userAccountDto().toEntity());

        sut.updateArticle(differentArticleId, dto);
        then(articleRepository).should().getReferenceById(differentArticleId);
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(hashtagService).shouldHaveNoInteractions();
    }

    /////* DELETE */
    @DisplayName("ArticleID -> Delete Article")
    @Test
    void givenArticleId_thenDeletesArticle(){
        Long articleId = 1L;
        String userId = "MJ";
        given(articleRepository.getReferenceById(articleId)).willReturn(createArticle());
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId,userId);
        willDoNothing().given(articleRepository).flush();
        willDoNothing().given(hashtagService).deleteHashtagWithoutArticles(any());

        sut.deleteArticle(1L,userId);
        then(articleRepository).should().getReferenceById(articleId);
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId,userId);
        then(articleRepository).should().flush();
        then(hashtagService).should(times(2)).deleteHashtagWithoutArticles(any());
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
        then(hashtagRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @DisplayName("Searching hashtag -> ArticlesPage")
    @Test
    void givenHashtag_whenSuccessSearching_thenReturnsArticlesPage() {
        String hashtagName = "java";
        Pageable pageable = Pageable.ofSize(20);
        Article expectedArticle = createArticle();
        given(articleRepository.findByHashtagNames(List.of(hashtagName),pageable)).willReturn(new PageImpl<>(List.of(expectedArticle),pageable,1));

        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);
        assertThat(articles).isEqualTo(new PageImpl<>(List.of(ArticleDto.from(expectedArticle)),pageable,1));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }

    @DisplayName("없는 hashtag -> emptyPage")
    @Test
    void givenNonexistentHashtag_whenSearchingArticlesViaHashtag_thenReturnsEmptyPage() {
        String hashtagName = "해시태그없음";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByHashtagNames(List.of(hashtagName), pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ArticleDto> articles = sut.searchArticlesViaHashtag(hashtagName, pageable);
        assertThat(articles).isEqualTo(Page.empty(pageable));
        then(articleRepository).should().findByHashtagNames(List.of(hashtagName), pageable);
    }


    @DisplayName("Hashtag List -> 존재하는 HashTags")
    @Test
    void givenNothing_whenCalling_thenReturnsHashtags() {
        Article article = createArticle();
        List<String> expectedHashtags = List.of("java", "spring", "boot");
        given(hashtagRepository.findAllHashtagNames()).willReturn(expectedHashtags);

        List<String> actualHashtags = sut.getHashtags();
        assertThat(actualHashtags).isEqualTo(expectedHashtags);
        then(hashtagRepository).should().findAllHashtagNames();
    }
    private UserAccount createUserAccount() {
        return createUserAccount("MJ");
    }

    private UserAccount createUserAccount(String userId) {
        return UserAccount.of(
                userId, "pw", "MJ@email.com", "MJ", null);
    }

    private Article createArticle() {
        return createArticle(1L);
    }

    private Article createArticle(Long id) {
        Article article = Article.of(createUserAccount(), "title", "content");
        article.addHashtags(Set.of(
                createHashtag(1L, "java"),
                createHashtag(2L, "spring")));
        ReflectionTestUtils.setField(article, "id", id);
        return article;
    }

    private Hashtag createHashtag(String hashtagName) {
        return createHashtag(1L, hashtagName);
    }

    private Hashtag createHashtag(Long id, String hashtagName) {
        Hashtag hashtag = Hashtag.of(hashtagName);
        ReflectionTestUtils.setField(hashtag, "id", id);
        return hashtag;
    }

    private HashtagDto createHashtagDto() {
        return HashtagDto.of("java");
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content");
    }

    private ArticleDto createArticleDto(String title, String content) {
        return ArticleDto.of(1L, createUserAccountDto(), title, content, null, LocalDateTime.now(), "MJ", LocalDateTime.now(), "MJ");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ", "pw", "MJ@mail.com", "MJ", "memo", LocalDateTime.now(), "MJ", LocalDateTime.now(), "MJ");
    }
}