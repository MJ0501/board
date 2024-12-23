package com.springboot.board.config;

import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.service.UserAccountService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;

@Import(SecurityConfig.class)
public class TestSecurityConfig {

    @MockBean private UserAccountService userAccountService;

    @BeforeTestMethod
    void securitySetUp() {
        given(userAccountService.searchUser(anyString())).willReturn(Optional.of(createUserAccountDto()));
        given(userAccountService.saveUser(anyString(), anyString(),anyString(),anyString(),anyString())).willReturn(createUserAccountDto());
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "testId",
                "pw",
                "test@email.com",
                "test-nick",
                "test-memo"
        );
    }
}