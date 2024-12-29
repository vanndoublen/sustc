package io.pubmed.controller;

import io.pubmed.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keywords")
public class KeywordController {

    @Autowired
    private KeywordService keywordService;

    @GetMapping("/articles/{keyword}")
    public ResponseEntity<int[]> getArticleCountByKeyword(@PathVariable String keyword) {
        try {
            int[] counts = keywordService.getArticleCountByKeywordInPastYears(keyword);
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new int[0]);
        }
    }
}