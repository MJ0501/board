package com.springboot.board.controller;

import com.springboot.board.service.ArticleService;
import com.springboot.board.service.PaginationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("View 컨트롤러 - 인증")
@WebMvcTest(AuthControllerTest.EmptyController.class)
class AuthControllerTest {

    private final MockMvc mvc;

    @MockBean
    private ArticleService articleService;
    @MockBean
    private PaginationService paginationService;

    AuthControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }


    @DisplayName("[GET] /login")
    @Test
    void whenLogIn_thenReturnsLogInView() throws Exception {
        mvc.perform(
                get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        then(articleService).shouldHaveNoInteractions();
    }

    /**
     * 어떤 컨트롤러도 필요하지 않은 테스트임을 나타내기 위해 테스트용 빈 컴포넌트를 사용함.
     */
    @TestComponent
    static class EmptyController {}

}
