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
        ArticleComment expected= createArticleComment("content");
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(expected));

        List<ArticleCommentDto> actual =  sut.searchArticleComments(articleId);
        assertThat(actual).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("content", expected.getContent());
        then(articleCommentRepository).should().findByArticle_Id(articleId);
    }

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
        then(articleCommentRepository).should().save(any(ArticleComment.class));
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
        article.addHashtags(Set.of(createHashtag(article)));
        return article;
    }

    private Hashtag createHashtag(Article article) {
        return Hashtag.of("java");
    }

    private ArticleComment createArticleComment(String content) {
        return ArticleComment.of(createArticle(),createUserAccount(),content);
    }


    private UserAccount createUserAccount() {
        return UserAccount.of("MJ", "pw", "MJ@mail.com", "MJ", null);
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ","pw","MJ@mail.com","MJ","WOOKiiiii",LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }

    private ArticleCommentDto createArticleCommentDto(String content) {
        return ArticleCommentDto.of(1L,1L,createUserAccountDto(),content,LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }

}