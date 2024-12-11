package com.springboot.board.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/articles")
@Controller
public class ArticleController {
    @GetMapping
    public String articles(ModelMap map){
        map.addAttribute("articles", List.of());
        return "articles/index";
    }
    @GetMapping("/{articleId}")
    public String article(@PathVariable Long articleId, ModelMap map) {
        map.addAttribute("article", "article"); // TODO: 구현할 때 여기에 실제 데이터를 넣어줘야 한다
        map.addAttribute("articleComments", List.of());
        return "articles/detail";
    }
    @GetMapping("/search-hashtag")
    public String searchHashtag(@RequestParam(required = false) String searchValue,
                                @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                ModelMap map) {
        //TODO: search-hashtag view controller
        return "articles/search-hashtag";
    }
}
