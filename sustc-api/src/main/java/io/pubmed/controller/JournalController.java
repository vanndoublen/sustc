package io.pubmed.controller;

import io.pubmed.dto.Journal;
//import io.pubmed.dto.JournalUpdateRequest; // no need this class
import io.pubmed.service.JournalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @GetMapping("/impact-factor")
    public ResponseEntity<Double> getImpactFactor(
            @RequestParam String title,
            @RequestParam int year) {
        try {
            double impactFactor = journalService.getImpactFactor(title, year);
            return ResponseEntity.ok(impactFactor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0.0);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Boolean> updateJournalName(
            @RequestBody Map<String, Object> request) {
        try {
            Journal journal = new Journal();
            journal.setId((String) request.get("journal_id"));
            journal.setTitle((String) request.get("journal_title"));
            int year = (Integer) request.get("year");
            String newName = (String) request.get("newName");
            String newId = (String) request.get("newId");

            boolean success = journalService.updateJournalName(journal, year, newName, newId);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}