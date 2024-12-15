package com.springboot.board.controller;

import com.springboot.board.dto.UserAccountDto;
import com.springboot.board.dto.request.ArticleCommentRequest;
import com.springboot.board.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class ArticleCommentController {
    private final ArticleCommentService articleCommentService;

    // Create Comment
    @PostMapping("/new")
    public String createNewArticleComment(ArticleCommentRequest articleCommentRequest){
        //TODO 인증 구현하면 인증 정보 넘기기
        articleCommentService.saveArticleComment(articleCommentRequest.toDto(UserAccountDto.of(
                "MJ","m0501","MJ@mail.com","MJ","임시접근관리자")));
        return "redirect:/articles/"+ articleCommentRequest.articleId();
    }

    // Delete Comment
    @PostMapping("/{commentId}/delete")
    public String deleteArticleComment(@PathVariable Long commentId, Long articleId){
        //TODO 인증 구현하면, 인증정보 넣기
        articleCommentService.deleteArticleComment(commentId);
        return "redirect:/articles/"+articleId;
    }

}
