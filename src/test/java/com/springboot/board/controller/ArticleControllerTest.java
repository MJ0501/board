package com.springboot.board.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("게시글/게시판 View")
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @Autowired
    private final MockMvc mvc;

    public ArticleControllerTest(MockMvc mvc){
        this.mvc = mvc;
    }

    @DisplayName("GET/게시글 목록")
    @Test
    public void whenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
        mvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("articles"));
    }
    @DisplayName("GET/게시글 상세 페이지")
    @Test
    public void whenRequestingArticleView_thenReturnsArticleView() throws Exception {
        mvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("article"));
    }
    @DisplayName("GET/게시글 검색 페이지")
    @Test
    public void whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
        mvc.perform(get("/articles/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
    @DisplayName("GET/게시글 해시태그 검색 페이지")
    @Test
    public void whenRequestingArticleHashTagSearchView_thenReturnsArticleHashTagSearchView() throws Exception {
        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }
}