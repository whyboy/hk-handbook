package com.hkhandbook.content;

import java.util.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import static com.hkhandbook.content.ContentValidator.*;

@RestController
@RequestMapping("/api/guides")
public class ContentController {
    private final ContentCatalog catalog;
    public ContentController(ContentCatalog catalog) { this.catalog = catalog; }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam MultiValueMap<String, String> params) {
        if (!Set.of("category", "audience", "q", "page", "size").containsAll(params.keySet()) || params.values().stream().anyMatch(v -> v.size() != 1)) return badRequest();
        String category = param(params, "category", ""), audience = param(params, "audience", ""), q = param(params, "q", "");
        if ((!category.isEmpty() && !CATEGORIES.contains(category)) || (!audience.isEmpty() && !AUDIENCES.contains(audience)) || q.codePointCount(0, q.length()) > 100) return badRequest();
        int page, size;
        try {
            String rawPage = param(params, "page", "1"), rawSize = param(params, "size", "20");
            if (!rawPage.matches("[1-9][0-9]*") || !rawSize.matches("[1-9][0-9]*")) return badRequest();
            page = Integer.parseInt(rawPage); size = Integer.parseInt(rawSize);
            if (size > 50 || page > 1000000) return badRequest();
        } catch (NumberFormatException e) { return badRequest(); }
        String query = q.toLowerCase(Locale.ROOT);
        var matches = catalog.visible().stream()
            .filter(a -> category.isEmpty() || value(a, "category").equals(category))
            .filter(a -> audience.isEmpty() || contains(a.get("audiences"), audience))
            .map(a -> ContentSearch.match(a, query))
            .filter(m -> query.isEmpty() || m.score() > 0)
            .sorted(query.isEmpty()
                ? Comparator.comparingInt(m -> catalog.recommendationRank(m.article()))
                : Comparator.comparingInt(ContentSearch.Match::score).reversed()).toList();
        long offset = (long) (page - 1) * size;
        var items = matches.stream().skip(offset).limit(size).map(m -> {
            var item = ContentCatalog.summary(m.article());
            if (!m.excerpt().isEmpty()) item.put("matchExcerpt", m.excerpt());
            return item;
        }).toList();
        return response(200, Map.of("items", items, "total", matches.size(), "page", page, "size", size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable String id, @RequestParam MultiValueMap<String, String> params) {
        if (!params.isEmpty()) return badRequest();
        JsonNode article = catalog.find(id);
        if (article == null) return missing();
        if (value(article, "status").equals("withdrawn")) return response(410, Map.of("code", "CONTENT_WITHDRAWN", "id", id, "message", "内容已下架", "returnPath", "/pages/guides/index"));
        if (!catalog.visible(article)) return missing();
        return response(200, ContentCatalog.detail(article));
    }
    private static String param(MultiValueMap<String, String> p, String key, String fallback) { String v = p.getFirst(key); return v == null ? fallback : v.strip(); }
    private static boolean contains(JsonNode values, String value) { for (var item : values) if (item.stringValue().equals(value)) return true; return false; }
    private static ResponseEntity<?> badRequest() { return response(400, Map.of("code", "INVALID_QUERY", "message", "查询参数无效")); }
    private static ResponseEntity<?> missing() { return response(404, Map.of("code", "CONTENT_UNAVAILABLE", "message", "内容不存在或暂不可用")); }
    private static ResponseEntity<?> response(int status, Object body) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(body);
    }
}
