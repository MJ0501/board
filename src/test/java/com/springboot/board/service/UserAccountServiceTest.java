package com.springboot.board.service;

import com.springboot.board.domain.UserAccount;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("비즈니스로직 - 회원")
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {
    @InjectMocks
    private UserAccountService sut;

    @Mock
    private UserAccountRepository userAccountRepository;

    @DisplayName("[Search]존재하는 회원 ID -> 회원 데이터를 Optional로 반환")
    @Test
    void givenExistentUserId_whenSearching_thenReturnsOptionalUserData() {
        String username = "uno";
        given(userAccountRepository.findById(username)).willReturn(Optional.of(createUserAccount(username)));

        Optional<UserAccountDto> result = sut.searchUser(username);
        assertThat(result).isPresent();
        then(userAccountRepository).should().findById(username);
    }

    @DisplayName("[Search] 존재하지 않는 회원 ID -> 비어있는 Optional을 반환")
    @Test
    void givenNonexistentUserId_whenSearching_thenReturnsOptionalUserData() {
        String username = "wrong-user";
        given(userAccountRepository.findById(username)).willReturn(Optional.empty());

        Optional<UserAccountDto> result = sut.searchUser(username);
        assertThat(result).isEmpty();
        then(userAccountRepository).should().findById(username);
    }

    @DisplayName("UserInfo -> SaveUserInfo -> 가입시키고 해당 회원 데이터를 리턴한다.")
    @Test
    void givenUserParams_whenSaving_thenSavesUserAccount() {
        UserAccount userAccount = createUserAccount("uno");
        UserAccount savedUserAccount = createSigningUpUserAccount("uno");
        given(userAccountRepository.save(userAccount)).willReturn(savedUserAccount);

        UserAccountDto result = sut.saveUser(
                userAccount.getUserId(),
                userAccount.getUserPassword(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getMemo());
        assertThat(result)
                .hasFieldOrPropertyWithValue("userId", userAccount.getUserId())
                .hasFieldOrPropertyWithValue("userPassword", userAccount.getUserPassword())
                .hasFieldOrPropertyWithValue("email", userAccount.getEmail())
                .hasFieldOrPropertyWithValue("nickname", userAccount.getNickname())
                .hasFieldOrPropertyWithValue("memo", userAccount.getMemo())
                .hasFieldOrPropertyWithValue("createdBy", userAccount.getUserId())
                .hasFieldOrPropertyWithValue("modifiedBy", userAccount.getUserId());
        then(userAccountRepository).should().save(userAccount);
    }

    private UserAccount createUserAccount(String username) {
        return createUserAccount(username, null);
    }

    private UserAccount createSigningUpUserAccount(String username) {
        return createUserAccount(username, username);
    }

    private UserAccount createUserAccount(String username, String createdBy) {
        return UserAccount.of(
                username,
                "password",
                "e@mail.com",
                "nickname",
                "memo",
                createdBy
        );
    }
}