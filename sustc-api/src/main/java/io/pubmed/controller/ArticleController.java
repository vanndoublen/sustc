package io.pubmed.controller;

import io.pubmed.dto.Article;
import io.pubmed.service.ArticleService;
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
@RequestMapping("/api/articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @GetMapping("/{id}/citations/{year}")
    public ResponseEntity<Integer> getCitations(@PathVariable int id, @PathVariable int year) {
        try {
            int citations = articleService.getArticleCitationsByYear(id, year);
            return ResponseEntity.ok(citations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/impact-factor")
    public ResponseEntity<Double> addArticleAndUpdateIF(@RequestBody Article article) {
        try {
            //convert util.Date to sql.Date
            if (article.getCreated() != null) {
                article.setCreated(new java.sql.Date(article.getCreated().getTime()));
            }
            if (article.getCompleted() != null) {
                article.setCompleted(new java.sql.Date(article.getCompleted().getTime()));
            }

            double impactFactor = articleService.addArticleAndUpdateIF(article);
            return ResponseEntity.ok(impactFactor);
        } catch (Exception e) {
            System.err.println("Error calculating impact factor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0.0);
        }
    }


}