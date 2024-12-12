package com.springboot.board.service;

import com.springboot.board.domain.ArticleComment;
import com.springboot.board.dto.ArticleCommentDto;
import com.springboot.board.repository.ArticleCommentRepository;
import com.springboot.board.repository.ArticleRepository;
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

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        return articleCommentRepository.findByArticle_Id(articleId)
                .stream().map(ArticleCommentDto::from).toList();
    }

    public void saveArticleComment(ArticleCommentDto dto) {
        try{
            articleCommentRepository.save(dto.toEntity(articleRepository.getReferenceById(dto.articleId())));
        }catch(EntityNotFoundException e){
            log.warn("댓글 저장할 수 없습니다. 게시글이 존재하지 않음. dto:{} ",dto);
        }
    }

    public void updateArticleComment(ArticleCommentDto dto) {
        try{
            ArticleComment articleComment = articleCommentRepository.getReferenceById(dto.id());
            if(dto.content() != null) {
                articleComment.setContent(dto.content());
            }
        }catch (EntityNotFoundException e){
            log.warn("댓글 Update 실패. 댓글이 존재하지 않음. dto:{} ",dto);
        }
    }

    public void deleteArticleComment(Long articleCommentId) {
        articleCommentRepository.deleteById(articleCommentId);
    }
}
