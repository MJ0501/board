package com.springboot.board.service;

import com.springboot.board.domain.Article;
import com.springboot.board.domain.Hashtag;
import com.springboot.board.domain.UserAccount;
import com.springboot.board.domain.constant.SearchType;
import com.springboot.board.dto.ArticleDto;
import com.springboot.board.dto.ArticleWithCommentsDto;
import com.springboot.board.repository.ArticleRepository;
import com.springboot.board.repository.HashtagRepository;
import com.springboot.board.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {
    private final HashtagService hashtagService;
    private final HashtagRepository hashtagRepository;
    private final ArticleRepository articleRepository;
    private final UserAccountRepository userAccountRepository;

    /* Article CRUD */
    public void saveArticle(ArticleDto dto) {
        UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());
        Set<Hashtag> hashtags = renewHashtagsFromContent(dto.content());

        Article article = dto.toEntity(userAccount);
        article.addHashtags(hashtags);
        articleRepository.save(article);
    }

    // Articles
    @Transactional(readOnly = true)
    public ArticleDto getArticle(Long articleId){
        return articleRepository.findById(articleId).map(ArticleDto::from)
                .orElseThrow(()->new EntityNotFoundException("게시글이 없습니다. articleId: "+articleId));
    }
    // Article 단건 조회(detail) : with Comments
    @Transactional(readOnly = true)
    public ArticleWithCommentsDto getArticleWithComments(Long articleId) {
        return articleRepository.findById(articleId).map(ArticleWithCommentsDto::from)
                .orElseThrow(()-> new EntityNotFoundException("해당 게시글이 없습니다. articleId: " + articleId));
    }

    public void updateArticle(Long articleId, ArticleDto dto) {
        try{
            Article article = articleRepository.getReferenceById(articleId);
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().userId());

            if(article.getUserAccount().equals(userAccount)){
                if(dto.title() != null) {article.setTitle(dto.title());}
                if(dto.content() != null) {article.setContent(dto.content());}

                Set<Long> hashtagIds = article.getHashtags().stream().map(Hashtag::getId)
                        .collect(Collectors.toUnmodifiableSet());
                article.clearHashtags();
                articleRepository.flush();
                hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);
                Set<Hashtag> hashtags = renewHashtagsFromContent(dto.content());
                article.addHashtags(hashtags);
            }
        }catch(EntityNotFoundException e){
            log.warn("게시글 수정을 실패했습니다. 수정하는 데 필요한 정보가 없습니다. {}",e.getLocalizedMessage());
        }
    }

    public void deleteArticle(Long articleId, String userId) {
        Article article = articleRepository.getReferenceById(articleId);
        Set<Long> hashtagIds = article.getHashtags().stream()
                .map(Hashtag::getId)
                .collect(Collectors.toUnmodifiableSet());
        articleRepository.deleteByIdAndUserAccount_UserId(articleId,userId);
        articleRepository.flush();
        hashtagIds.forEach(hashtagService::deleteHashtagWithoutArticles);
    }

    /* searching, pagination, sort 관련 */
    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if(searchType == null || searchKeyword.isEmpty()) {
            return articleRepository.findAll(pageable).map(ArticleDto::from);
        }
        return switch (searchType){
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
            case HASHTAG -> articleRepository.findByHashtagNames(Arrays.stream(searchKeyword.split(" ")).toList(),pageable).map(ArticleDto::from);
        };
    }

    public long getArticleCount() {
        return articleRepository.count();
    }

    /* hashtag 관련*/
    private Set<Hashtag> renewHashtagsFromContent(String content) {
        Set<String> hashtagNamesInContent = hashtagService.parseHashtagNames(content);
        Set<Hashtag> hashtags = hashtagService.findHashtagsByNames(hashtagNamesInContent);
        Set<String> existingHashtagNames = hashtags.stream()
                .map(Hashtag::getHashtagName)
                .collect(Collectors.toUnmodifiableSet());

        hashtagNamesInContent.forEach(newHashtagName -> {
            if (!existingHashtagNames.contains(newHashtagName)) {
                hashtags.add(Hashtag.of(newHashtagName));}
        });
        return hashtags;
    }

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticlesViaHashtag(String hashtagName, Pageable pageable) {
        if (hashtagName == null || hashtagName.isBlank()) {
            return Page.empty(pageable);
        }
        return articleRepository.findByHashtagNames(List.of(hashtagName), pageable).map(ArticleDto::from);
    }

    public List<String> getHashtags() {
        return hashtagRepository.findAllHashtagNames(); // TODO: HashtagService 로 이동을 고려해보자.
    }
}
