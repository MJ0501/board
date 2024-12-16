package com.springboot.board.config;

import com.springboot.board.domain.UserAccount;
import com.springboot.board.repository.UserAccountRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import java.util.Optional;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;

@Import(SecurityConfig.class)
public class TestSecurityConfig {

    @MockBean private UserAccountRepository userAccountRepository;

    @BeforeTestMethod
    void securitySetUp() {
        given(userAccountRepository.findById(anyString()))
                .willReturn(Optional.of(createUserAccountDto()));
    }

    private UserAccount createUserAccountDto() {
        return UserAccount.of(
                "testId",
                "pw",
                "test@email.com",
                "test-nick",
                "test-memo"
        );
    }

}