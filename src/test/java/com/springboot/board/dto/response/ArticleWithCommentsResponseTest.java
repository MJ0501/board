package com.springboot.board.dto.response;

import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.dto.HashtagDto;
import com.springboot.board.dto.UserAccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;

@DisplayName("DTO - 댓글을 포함한 게시글 응답 테스트")
class ArticleWithCommentsResponseTest {

    @DisplayName("Child X + Comment dto를 api 응답으로 변환할 때, Comment 정렬: CreatedAt Desc + CommentID ASC")
    @Test
    void givenArticleWithCommentsDtoWithoutChildComments_whenMapping_thenOrganizesCommentsWithCertainOrder() {
        LocalDateTime now = LocalDateTime.now();
        Set<ArticleCommentDto> articleCommentDtos = Set.of(
            createArticleCommentDto(1L, null, now),
            createArticleCommentDto(2L, null, now.plusDays(1L)),
            createArticleCommentDto(3L, null, now.plusDays(3L)),
            createArticleCommentDto(4L, null, now),
            createArticleCommentDto(5L, null, now.plusDays(5L)),
            createArticleCommentDto(6L, null, now.plusDays(4L)),
            createArticleCommentDto(7L, null, now.plusDays(2L)),
            createArticleCommentDto(8L, null, now.plusDays(7L)));
        ArticleWithCommentsDto input = createArticleWithCommentsDto(articleCommentDtos);

        ArticleWithCommentsResponse actual = ArticleWithCommentsResponse.from(input);
        assertThat(actual.articleCommentResponse())
            .containsExactly(
                createArticleCommentResponse(8L, null, now.plusDays(7L)),
                createArticleCommentResponse(5L, null, now.plusDays(5L)),
                createArticleCommentResponse(6L, null, now.plusDays(4L)),
                createArticleCommentResponse(3L, null, now.plusDays(3L)),
                createArticleCommentResponse(7L, null, now.plusDays(2L)),
                createArticleCommentResponse(2L, null, now.plusDays(1L)),
                createArticleCommentResponse(1L, null, now),
                createArticleCommentResponse(4L, null, now));
    }

    @DisplayName("게시글 + 댓글 dto를 api 응답으로 변환할 때, ParantComment, ChildComment 정렬확인")
    @Test
    void givenArticleWithCommentsDto_whenMapping_thenOrganizesParentAndChildCommentsWithCertainOrders() {
        LocalDateTime now = LocalDateTime.now();
        Set<ArticleCommentDto> articleCommentDtos = Set.of(
            createArticleCommentDto(1L, null, now),
            createArticleCommentDto(2L, 1L, now.plusDays(1L)),
            createArticleCommentDto(3L, 1L, now.plusDays(3L)),
            createArticleCommentDto(4L, 1L, now),
            createArticleCommentDto(5L, null, now.plusDays(5L)),
            createArticleCommentDto(6L, null, now.plusDays(4L)),
            createArticleCommentDto(7L, 6L, now.plusDays(2L)),
            createArticleCommentDto(8L, 6L, now.plusDays(7L)));
        ArticleWithCommentsDto input = createArticleWithCommentsDto(articleCommentDtos);

        ArticleWithCommentsResponse actual = ArticleWithCommentsResponse.from(input);
        assertThat(actual.articleCommentResponse())
            .containsExactly(
                createArticleCommentResponse(5L, null, now.plusDays(5)),
                createArticleCommentResponse(6L, null, now.plusDays(4)),
                createArticleCommentResponse(1L, null, now)
            )
            .flatExtracting(ArticleCommentResponse::childComments)
            .containsExactly(
                createArticleCommentResponse(7L, 6L, now.plusDays(2L)),
                createArticleCommentResponse(8L, 6L, now.plusDays(7L)),
                createArticleCommentResponse(4L, 1L, now),
                createArticleCommentResponse(2L, 1L, now.plusDays(1L)),
                createArticleCommentResponse(3L, 1L, now.plusDays(3L)));
    }

    @DisplayName("[N차 대댓글]게시글 + 댓글 dto를 api 응답으로 변환할 때, 부모 자식 관계 깊이(depth)는 제한이 없다.")
    @Test
    void givenArticleWithCommentsDto_whenMapping_thenOrganizesParentAndChildCommentsWithoutDepthLimit() {
        LocalDateTime now = LocalDateTime.now();
        Set<ArticleCommentDto> articleCommentDtos = Set.of(
            createArticleCommentDto(1L, null, now),
            createArticleCommentDto(2L, 1L, now.plusDays(1L)),
            createArticleCommentDto(3L, 2L, now.plusDays(2L)),
            createArticleCommentDto(4L, 3L, now.plusDays(3L)),
            createArticleCommentDto(5L, 4L, now.plusDays(4L)),
            createArticleCommentDto(6L, 5L, now.plusDays(5L)),
            createArticleCommentDto(7L, 6L, now.plusDays(6L)),
            createArticleCommentDto(8L, 7L, now.plusDays(7L)));
        ArticleWithCommentsDto input = createArticleWithCommentsDto(articleCommentDtos);

        ArticleWithCommentsResponse actual = ArticleWithCommentsResponse.from(input);
        Iterator<ArticleCommentResponse> iterator = actual.articleCommentResponse().iterator();
        long i = 1L;
        while (iterator.hasNext()) {
            ArticleCommentResponse articleCommentResponse = iterator.next();
            assertThat(articleCommentResponse)
                .hasFieldOrPropertyWithValue("id", i)
                .hasFieldOrPropertyWithValue("parentCommentId", i == 1L ? null : i - 1L)
                .hasFieldOrPropertyWithValue("createdAt", now.plusDays(i - 1L));

            iterator = articleCommentResponse.childComments().iterator();
            i++;
        }
    }

    private ArticleWithCommentsDto createArticleWithCommentsDto(Set<ArticleCommentDto> articleCommentDtos) {
        return ArticleWithCommentsDto.of(1L, createUserAccountDto(), articleCommentDtos,
            "title", "content", Set.of(HashtagDto.of("java")),
                LocalDateTime.now(), "MJ", LocalDateTime.now(), "MJ");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ", "pw", "MJ@mail.com", "MJ", "This is memo",
                LocalDateTime.now(), "MJ", LocalDateTime.now(), "MJ");
    }

    private ArticleCommentDto createArticleCommentDto(Long id, Long parentCommentId, LocalDateTime createdAt) {
        return ArticleCommentDto.of(id, 1L, createUserAccountDto(), parentCommentId,
                "test comment " + id, createdAt, "MJ", createdAt, "MJ");
    }

    private ArticleCommentResponse createArticleCommentResponse(Long id, Long parentCommentId, LocalDateTime createdAt) {
        return ArticleCommentResponse.of(id, "test comment " + id,
                        createdAt, "MJ@mail.com", "MJ", "MJ", parentCommentId);
    }

}