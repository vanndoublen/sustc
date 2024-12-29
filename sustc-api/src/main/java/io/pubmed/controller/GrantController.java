package io.pubmed.controller;



import io.pubmed.service.GrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grants")
public class GrantController {

    @Autowired
    private GrantService grantService;

    @GetMapping("/papers/{country}")
    public ResponseEntity<int[]> getCountryFundPapers(@PathVariable String country) {
        try {
            int[] papers = grantService.getCountryFundPapers(country);
            return ResponseEntity.ok(papers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new int[0]);
        }
    }
}