package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.ArticleComment;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.ArticleCommentRepository;
import com.springboot.board.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Disabled
@DisplayName("비즈니스로직 - Comment")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {
    @InjectMocks private ArticleCommentService sut;
    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleCommentRepository articleCommentRepository;


    @DisplayName("ArticleID 조회 -> 댓글리스트반환")
    @Test
    void givenArticleId_whenSearchingComments_thenReturnsComments(){
        //given
        Long articleId = 1L;
        ArticleComment expected= createArticleComment("content");
        given(articleCommentRepository.findByArticle_Id(articleId)).willReturn(List.of(expected));
        //when
        List<ArticleCommentDto> actual =  sut.searchArticleComments(articleId);
        //then
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
        given(articleCommentRepository.save(any(ArticleComment.class))).willReturn(null);

        //when
        sut.saveArticleComment(dto);
        // Then
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(articleCommentRepository).should().save(any(ArticleComment.class));

    }

    @DisplayName("댓글 저장을 시도했는데 맞는 게시글이 없으면, 경고 로그를 찍고 아무것도 안 한다.")
    @Test
    void givenNonexistentArticle_whenSavingArticleComment_thenLogsSituationAndDoesNothing() {
        // Given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleRepository.getReferenceById(dto.articleId())).willThrow(EntityNotFoundException.class);

        // When
        sut.saveArticleComment(dto);

        // Then
        then(articleRepository).should().getReferenceById(dto.articleId());
        then(articleCommentRepository).shouldHaveNoInteractions();
    }
    @DisplayName("CommentInfo -> Update Comment")
    @Test
    void givenArticleCommentInfo_whenUpdatingArticleComment_thenUpdatesArticleComment() {
        // Given
        String oldContent = "content";
        String updatedContent = "댓글";
        ArticleComment articleComment = createArticleComment(oldContent);
        ArticleCommentDto dto = createArticleCommentDto(updatedContent);
        given(articleCommentRepository.getReferenceById(dto.id())).willReturn(articleComment);

        // When
        sut.updateArticleComment(dto);

        // Then
        assertThat(articleComment.getContent())
                .isNotEqualTo(oldContent)
                .isEqualTo(updatedContent);
        then(articleCommentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("없는 댓글 정보를 수정하려고 하면, 경고 로그를 찍고 아무 것도 안 한다.")
    @Test
    void givenNonexistentArticleComment_whenUpdatingArticleComment_thenLogsWarningAndDoesNothing() {
        // Given
        ArticleCommentDto dto = createArticleCommentDto("댓글");
        given(articleCommentRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

        // When
        sut.updateArticleComment(dto);

        // Then
        then(articleCommentRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("CommentID -> Delete Comment")
    @Test
    void givenArticleCommentId_whenDeletingArticleComment_thenDeletesArticleComment() {
        // Given
        Long articleCommentId = 1L;
        willDoNothing().given(articleCommentRepository).deleteById(articleCommentId);

        // When
        sut.deleteArticleComment(articleCommentId);

        // Then
        then(articleCommentRepository).should().deleteById(articleCommentId);
    }


    private Article createArticle() {
        return Article.of(createUserAccount(),"title","content","#java");
    }

    private ArticleComment createArticleComment(String content) {
        return ArticleComment.of(Article.of(createUserAccount(),"title","content","hashtag"),createUserAccount(),content);
    }

    private UserAccount createUserAccount() {
        return UserAccount.of("MJ123", "pw", "MJ@mail.com", "MJ", null);
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ","pw","MJ@mail.com","MJ","WOOKiiiii",LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }
    private ArticleCommentDto createArticleCommentDto(String content) {
        return ArticleCommentDto.of(1L,1L,createUserAccountDto(),content,LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }





}