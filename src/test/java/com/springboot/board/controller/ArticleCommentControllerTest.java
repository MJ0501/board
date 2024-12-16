package com.springboot.board.controller;

import com.springboot.board.config.TestSecurityConfig;
import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.dto.request.ArticleCommentRequest;
import com.springboot.board.service.ArticleCommentService;
import com.springboot.board.util.FormDataEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("컨트롤러View - Comment")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(ArticleCommentController.class)
class ArticleCommentControllerTest {

    private final MockMvc mvc;
    @MockBean
    private ArticleCommentService articleCommentService;
    private final FormDataEncoder formDataEncoder;

    public ArticleCommentControllerTest(@Autowired MockMvc mvc, @Autowired FormDataEncoder formDataEncoder) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }

    // CREATE COMMENT
    @DisplayName("[POST]/comments/new : Save new Comment")
    @WithUserDetails(value = "testId", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void givenNewCommentInfo_whenRequesting_thenSavesNewComment() throws Exception {
        Long articleId=1L;
        ArticleCommentRequest articleCommentRequest = ArticleCommentRequest.of(articleId,"TESTTEST");
        willDoNothing().given(articleCommentService).saveArticleComment(any(ArticleCommentDto.class));

        mvc.perform(post("/comments/new")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .content(formDataEncoder.encode(articleCommentRequest))
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/"+articleId))
                .andExpect(redirectedUrl("/articles/"+articleId));
        then(articleCommentService).should().saveArticleComment(any(ArticleCommentDto.class));

    }
    // DELETE COMMENT
    @DisplayName("[GET]/comments/{commentId}/delete : DELETE COMMENT")
    @WithUserDetails(value = "testId", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void givenCommentId_thenDeletesComment() throws Exception {
        long articleId = 1L;
        long articleCommentId = 1L;
        String userId= "testId";
        willDoNothing().given(articleCommentService).deleteArticleComment(articleCommentId,userId);

        mvc.perform(post("/comments/"+articleCommentId+"/delete")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(formDataEncoder.encode(Map.of("articleId",articleId)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/articles/"+articleId))
                .andExpect(redirectedUrl("/articles/"+articleId));

        then(articleCommentService).should().deleteArticleComment(articleCommentId,userId);
    }

}