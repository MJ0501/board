package com.springboot.board.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("DATA REST 통합테스트 제외시킴")
@DisplayName("Data Rest Test - API 통합테스트")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class DataRestTest {
    private MockMvc mvc;
    @DisplayName("[api] 회원 관련 API 는 일체 제공하지 않는다.")
    @Test
    void givenNothing_whenRequestingUserAccounts_thenThrowsException() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/api/userAccounts")).andExpect(status().isNotFound());
        mvc.perform(post("/api/userAccounts")).andExpect(status().isNotFound());
        mvc.perform(put("/api/userAccounts")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/userAccounts")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/userAccounts")).andExpect(status().isNotFound());
        mvc.perform(head("/api/userAccounts")).andExpect(status().isNotFound());
    }


    public DataRestTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }
    @DisplayName("[api] 게시글 리스트 조회")
    @Test
    void whenRequestingArticles_thenReturnsArticlesJsonResponse() throws Exception {
        mvc.perform(get("/api/articles")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }
    @DisplayName("[api] 단일 게시글 조회")
    @Test
    void whenRequestingArticle_thenReturnsArticleJsonResponse() throws Exception {
        mvc.perform(get("/api/articles/1")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }
    @DisplayName("[api] 게시글 -> 댓글 리스트 조회")
    @Test
    void whenRequestingArticleCommentsFromArticle_thenReturnsArticleCommentsJsonResponse() throws Exception {
        mvc.perform(get("/api/articles/1/articleComments")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }
    @DisplayName("[api] 댓글 리스트 조회")
    @Test
    void whenRequestingArticleComments_thenReturnsArticleCommentsJsonResponse() throws Exception {
        mvc.perform(get("/api/articleComments")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }
    @DisplayName("[api] 단일 댓글 조회")
    @Test
    void whenRequestingArticleComment_thenReturnsArticleCommentJsonResponse() throws Exception {
        mvc.perform(get("/api/articleComments/1")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")));
    }
}
