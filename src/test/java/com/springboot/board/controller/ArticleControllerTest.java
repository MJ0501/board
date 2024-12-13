package com.springboot.board.controller;

import com.springboot.board.config.SecurityConfig;
import com.springboot.board.domain.constant.SearchType;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.service.ArticleService;
import com.springboot.board.service.PaginationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("컨트롤러View - Article")
@Import(SecurityConfig.class)
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    private final MockMvc mvc;
    @MockBean private ArticleService articleService;
    @MockBean private PaginationService paginationService;

    ArticleControllerTest(@Autowired MockMvc mvc){
        this.mvc = mvc;
    }

    @DisplayName("[GET]/articles/index")
    @Test
    void givenRequestingArticlesView_thenReturnsArticlesView() throws Exception {
        given(articleService.searchArticles(eq(null), eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPagingBarNumbers(anyInt(),anyInt())).willReturn(List.of(0,1,2,3,4));

        mvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("paginationBarNumbers"));
        then(articleService).should().searchArticles(eq(null), eq(null), any(Pageable.class));
        then(paginationService).should().getPagingBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("[Pagination, Sort] /articles/index")
    @Test
    void givenPagingAndSortingParams_whenSearchingArticlesPage_thenReturnsArticlesView() throws Exception {
        String sortName = "title";
        String direction = "desc";
        int pageNumber = 0;
        int pageSize = 5;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc(sortName)));
        List<Integer> barNumbers = List.of(1, 2, 3, 4, 5);

        given(articleService.searchArticles(null, null, pageable)).willReturn(Page.empty());
        given(paginationService.getPagingBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages())).willReturn(barNumbers);
        // When & Then
        mvc.perform(get("/articles").queryParam("page", String.valueOf(pageNumber))
                    .queryParam("size", String.valueOf(pageSize))
                    .queryParam("sort", sortName + "," + direction))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attribute("paginationBarNumbers", barNumbers))
                .andExpect(model().attributeExists("searchTypes"));
        then(articleService).should().searchArticles(null, null, pageable);
        then(paginationService).should().getPagingBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages());
    }

    @DisplayName("[GET]/articles/detail")
    @Test
    void whenRequestingArticleView_thenReturnsArticleView() throws Exception {
        Long articleId = 1L;
        given(articleService.getArticle(articleId)).willReturn(createArticleWithCommentsDto());

        mvc.perform(get("/articles/" + articleId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/detail"))
                .andExpect(model().attributeExists("article"))
                .andExpect(model().attributeExists("articleComments"));
        then(articleService).should().getArticle(articleId);
    }

    @DisplayName("[SearchKeyword] /articles/index")
    @Test
    public void givenSearchKeyword_whenSearchingArticlesView_thenReturnsArticlesView() throws Exception {
        SearchType searchType = SearchType.TITLE;
        String searchValue = "title";

        given(articleService.searchArticles(eq(searchType), eq(searchValue), any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPagingBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));
        // When & Then
        mvc.perform(get("/articles")
                    .queryParam("searchType", searchType.name())
                    .queryParam("searchValue", searchValue))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/index"))
                .andExpect(model().attributeExists("articles"))
                .andExpect(model().attributeExists("searchTypes"));
        then(articleService).should().searchArticles(eq(searchType), eq(searchValue), any(Pageable.class));
        then(paginationService).should().getPagingBarNumbers(anyInt(), anyInt());
    }
    @Disabled
    @DisplayName("[GET]/articles ")
    @Test
    void whenRequestingArticleSearchView_thenReturnsArticleSearchView() throws Exception {
        mvc.perform(get("/articles/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search"));
    }

    @DisplayName("[Hashtag -view]/articles/search-hashtag")
    @Test
    void whenRequestingArticleHashTagSearchView_thenReturnsArticleHashTagSearchView() throws Exception {
        List<String> hashtags = List.of("#java","#spring","#boot","#pink","#maron","#C++");
        given(articleService.searchArticlesViaHashtag(eq(null),any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(hashtags);

        mvc.perform(get("/articles/search-hashtag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles",Page.empty()))
                .andExpect(model().attribute("hashtags",hashtags))
                .andExpect(model().attributeExists("paginationBarNumbers"));
        then(articleService).should().searchArticlesViaHashtag(eq(null),any(Pageable.class));
        then(articleService).should().getHashtags();
    }
    @DisplayName("[Hashtag-searching]")
    @Test
    public void givenHashtag_whenRequestingArticleSearchHashtagView_thenReturnsArticleSearchHashtagView() throws Exception {
        String hashtag = "#java";

        given(articleService.searchArticlesViaHashtag(eq(hashtag), any(Pageable.class))).willReturn(Page.empty());
        given(articleService.getHashtags()).willReturn(List.of("#java","#spring","#boot"));
        given(paginationService.getPagingBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));
        // When & Then
        mvc.perform(get("/articles/search-hashtag").queryParam("searchValue", hashtag))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("articles/search-hashtag"))
                .andExpect(model().attribute("articles", Page.empty()))
                .andExpect(model().attributeExists("hashtags"))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.HASHTAG));
        then(articleService).should().searchArticlesViaHashtag(eq(hashtag), any(Pageable.class));
        then(articleService).should().getHashtags();
        then(paginationService).should().getPagingBarNumbers(anyInt(), anyInt());
    }

    private ArticleWithCommentsDto createArticleWithCommentsDto() {
        return ArticleWithCommentsDto.of(1L,createUserAccountDto(), Set.of(),"title","content","#java", LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of("MJ","pw","MJ@mail.com","MJ","WOOK",LocalDateTime.now(),"MJ",LocalDateTime.now(),"MJ");
    }
}