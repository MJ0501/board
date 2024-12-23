package com.springboot.board.service;

import com.springboot.board.domain.UserAccount;
import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public Optional<UserAccountDto> searchUser(String username){
        return userAccountRepository.findById(username).map(UserAccountDto::from);
    }

    public UserAccountDto saveUser(String username, String password, String email, String nickname, String memo){
        return UserAccountDto.from(userAccountRepository.save(UserAccount.of(username,password,email,nickname, memo, username)));
    }
}
