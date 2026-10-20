package com.hkhandbook.content;

import java.util.*;
import tools.jackson.databind.JsonNode;
import static com.hkhandbook.content.ContentValidator.value;

/** Literal, case-insensitive search over public editorial text only. */
final class ContentSearch {
    record Match(JsonNode article, int score, String excerpt) {}
    static Match match(JsonNode article, String query) {
        if (query.isEmpty()) return new Match(article, 0, "");
        String needle = query.toLowerCase(Locale.ROOT);
        int score = contains(value(article, "title"), needle) ? 3 : 0;
        String excerpt = "";
        for (var keyword : article.get("keywords"))
            if (contains(keyword.stringValue(), needle)) score = Math.max(score, 2);
        String summary = value(article, "summary");
        if (contains(summary, needle)) { score = Math.max(score, 2); excerpt = snippet(summary, needle); }
        for (String section : List.of("applicability", "materials", "steps", "costsAndTime", "warnings")) {
            for (var block : article.get("sections").get(section).get("blocks")) {
                String text = value(block, "text");
                if (contains(text, needle)) {
                    score = Math.max(score, 1);
                    if (excerpt.isEmpty()) excerpt = snippet(text, needle);
                }
            }
        }
        return new Match(article, score, excerpt);
    }
    private static boolean contains(String text, String needle) { return text.toLowerCase(Locale.ROOT).contains(needle); }
    private static String snippet(String text, String needle) {
        // Locate on original code-point boundaries: lowercasing can change UTF-16 length.
        int match = 0;
        for (int i = 0; i < text.length(); i = text.offsetByCodePoints(i, 1)) {
            if (text.substring(i).toLowerCase(Locale.ROOT).startsWith(needle)) { match = i; break; }
        }
        int start = text.offsetByCodePoints(match, -Math.min(24, text.codePointCount(0, match)));
        int end = text.offsetByCodePoints(start, Math.min(160, text.codePointCount(start, text.length())));
        return (start > 0 ? "…" : "") + text.substring(start, end) + (end < text.length() ? "…" : "");
    }
}
