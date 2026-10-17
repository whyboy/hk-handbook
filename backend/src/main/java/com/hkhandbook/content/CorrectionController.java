package com.hkhandbook.content;

import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/guides/{id}/corrections")
public class CorrectionController {
    private final JdbcTemplate jdbc;
    private final GuideController guides;

    public CorrectionController(JdbcTemplate jdbc, GuideController guides) {
        this.jdbc = jdbc;
        this.guides = guides;
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS corrections (
                id TEXT PRIMARY KEY, guide_id TEXT NOT NULL, kind TEXT NOT NULL,
                message TEXT NOT NULL, reference_url TEXT NOT NULL, created_at TEXT NOT NULL,
                preview INTEGER NOT NULL DEFAULT 0
            )
            """);
    }

    public record Submission(String kind, String message, String referenceUrl) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> submit(@PathVariable String id, @RequestBody Submission input,
                                      @RequestParam(defaultValue = "false") boolean preview) {
        guides.detail(id, preview);
        if (input.kind() == null || !Set.of("outdated", "incorrect", "other").contains(input.kind())
                || input.message() == null || input.message().isBlank() || input.message().length() > 2000
                || (input.referenceUrl() != null && (input.referenceUrl().length() > 500
                || (!input.referenceUrl().isBlank() && !input.referenceUrl().matches("https://[^\\s]+"))))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请检查纠错类型、内容及 HTTPS 来源链接");
        }
        String correctionId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO corrections(id, guide_id, kind, message, reference_url, created_at, preview) VALUES (?, ?, ?, ?, ?, ?, ?)",
            correctionId, id, input.kind(), input.message().strip(), input.referenceUrl() == null ? "" : input.referenceUrl().strip(), Instant.now().toString(), preview ? 1 : 0);
        return Map.of("id", correctionId, "status", "received");
    }
}
