package com.springboot.board.controller;

import com.springboot.board.dto.request.ArticleCommentRequest;
import com.springboot.board.dto.security.BoardPrincipal;
import com.springboot.board.service.ArticleCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String createNewArticleComment(@AuthenticationPrincipal BoardPrincipal boardPrincipal, ArticleCommentRequest articleCommentRequest){
        articleCommentService.saveArticleComment(articleCommentRequest.toDto(boardPrincipal.toDto()));
        return "redirect:/articles/"+ articleCommentRequest.articleId();
    }

    // Delete Comment
    @PostMapping("/{commentId}/delete")
    public String deleteArticleComment(@PathVariable Long commentId, @AuthenticationPrincipal BoardPrincipal boardPrincipal, Long articleId){
        articleCommentService.deleteArticleComment(commentId,boardPrincipal.getUsername());
        return "redirect:/articles/"+articleId;
    }

}
