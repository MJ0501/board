package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.ArticleComment;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.repository.ArticleCommentRepository;
import com.springboot.board.repository.ArticleRepository;
import com.springboot.board.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {
    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final UserAccountRepository userAccountRepository;

    //  CREATE COMMENT
    public void saveArticleComment(ArticleCommentDto dto) {
        try{
            Article article = articleRepository.getReferenceById(dto.articleId());
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
            ArticleComment articleComment = dto.toEntity(article, userAccount);

            if (dto.parentCommentId() != null) {
                ArticleComment parentComment = articleCommentRepository.getReferenceById(dto.parentCommentId());
                parentComment.addChildComment(articleComment);
            } else {
                articleCommentRepository.save(articleComment);
            }
        }catch(EntityNotFoundException e){
            log.warn("댓글 저장할 수 없습니다.댓글 작성에 필요한 정보를 찾을 수 없습니다. {} ",e.getLocalizedMessage());
        }
    }

    //  READ COMMENT
    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        return articleCommentRepository.findByArticle_Id(articleId)
                .stream().map(ArticleCommentDto::from).toList();
    }

    // DELETE COMMENT
    public void deleteArticleComment(Long articleCommentId, String userId) {
        articleCommentRepository.deleteByIdAndUserAccount_UserId(articleCommentId, userId);
    }
}
