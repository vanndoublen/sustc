package io.pubmed.controller;

import io.pubmed.dto.Author;
import io.pubmed.service.AuthorService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @PostMapping("/articles")
    public ResponseEntity<int[]> getArticlesByAuthor(@RequestBody Author author) {
        try {
            int[] articles = authorService.getArticlesByAuthorSortedByCitations(author);
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new int[0]);
        }
    }

    @PostMapping("/journal")
    public ResponseEntity<String> getJournalWithMostArticles(@RequestBody Author author) {
        try {
            String journal = authorService.getJournalWithMostArticlesByAuthor(author);
            return ResponseEntity.ok(journal);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }
}