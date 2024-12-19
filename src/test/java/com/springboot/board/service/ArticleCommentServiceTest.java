package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.ArticleComment;
import com.springboot.board.domain.Hashtag;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.ArticleCommentRepository;
import com.springboot.board.repository.ArticleRepository;
import com.springboot.board.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스로직 - Comment")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
    @InjectMocks
    private ArticleCommentService sut;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleCommentRepository articleCommentRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @DisplayName("ArticleID 조회 -> 댓글리스트반환")
    @Test
    void givenArticleId_whenSearchingComments_thenReturnsComments(){
        Long articleId = 1L;

        ArticleComment expectedParentComment = createArticleComment(1L, "parent content");
        ArticleComment expectedChildComment = createArticleComment(2L, "child content");
        expectedChildComment.setParentCommentId(expectedParentComment.getId());
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(
                expectedParentComment, expectedChildComment));

        List<ArticleCommentDto> actual =  sut.searchArticleComments(articleId);
        assertThat(actual).hasSize(2);
        assertThat(actual).extracting("id","articleId","parentCommentId","content")
                        .containsExactlyInAnyOrder(
                                tuple(1L, 1L, null, "parent content"),
                                tuple(2L, 1L, 1L, "child content")
                        );
        then(articleCommentRepository).should().findByArticle_Id(articleId);
    }
    // 댓글 저장
    @DisplayName("CommentInfo -> Save Comment")
    @Test
    void givenCommentInfo_whenSavingComment_thenReturnsComment(){
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);

        sut.saveArticleComment(dto);
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
        then(articleCommentRepository).should(never()).getReferenceById(anyLong());
        then(articleCommentRepository).should().save(any(ArticleComment.class));
    }
    // 대댓글 저장
    @DisplayName("Parent ID + Child INFO -> SAVE ChildComment")
    @Test
    void givenParentCommentIdAndArticleCommentInfo_whenSaving_thenSavesChildComment() {
        Long parentCommentId = 1L;
        ArticleComment parent = createArticleComment(parentCommentId, "댓글");
        ArticleCommentDto child = createArticleCommentDto(parentCommentId, "대댓글");
        given(articleRepository.getReferenceById(child.articleId())).willReturn(createArticle());
        given(userAccountRepository.getReferenceById(child.userAccountDto().userId())).willReturn(createUserAccount());
        given(articleCommentRepository.getReferenceById(child.parentCommentId())).willReturn(parent);

        sut.saveArticleComment(child);

        assertThat(child.parentCommentId()).isNotNull();
        then(articleRepository).should().getReferenceById(child.articleId());
        then(userAccountRepository).should().getReferenceById(child.userAccountDto().userId());
        then(articleCommentRepository).should().getReferenceById(child.parentCommentId());
        then(articleCommentRepository).should(never()).save(any(ArticleComment.class));
    }


    @DisplayName("article x -> save x -> 경고 로그")
    @Test
    void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);

        sut.saveArticleComment(dto);
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(articleCommentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("CommentID -> Delete Comment")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        Long articleCommentId = 1L;
        String userId = "MJ";
        willDoNothing().given(articleCommentRepository).deleteByIdAndUserAccount_UserId(articleCommentId, userId);

        sut.deleteArticleComment(articleCommentId, userId);
        then(articleCommentRepository).should().deleteByIdAndUserAccount_UserId(articleCommentId,userId);
    }



    private Article createArticle(){
        Article article = Article.of(createUserAccount(),"title","content");
        ReflectionTestUtils.setField(article, "id", 1L);
        article.addHashtags(Set.of(createHashtag(article)));
        return article;
    }

    private Hashtag createHashtag(Article article) {
        return Hashtag.of("java");
    }

    private ArticleComment createArticleComment(Long id, String content) {
        ArticleComment articleComment = ArticleComment.of(createArticle(),createUserAccount(),content);
        ReflectionTestUtils.setField(articleComment,"id",id);
        return articleComment;
    }

    private ArticleCommentDto createArticleCommentDto(String content) {
        return createArticleCommentDto(null, content);
    }
    private ArticleCommentDto createArticleCommentDto(Long parentCommentId, String content) {
        return createArticleCommentDto(1L, parentCommentId, content);
    }
    private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, String content) {
        return ArticleCommentDto.of(id, 1L, createUserAccountDto(),parentCommentId,content,LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }

    private UserAccount createUserAccount() {
        return UserAccount.of("MJ", "pw", "MJ@mail.com", "MJ", null);
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ","pw","MJ@mail.com","MJ","WOOKiiiii",LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }



}