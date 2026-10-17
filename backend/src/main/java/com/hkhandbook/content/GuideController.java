package com.hkhandbook.content;

import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/guides")
public class GuideController {
    private final GuideCatalog catalog;
    private final boolean previewEnabled;

    public GuideController(GuideCatalog catalog, @Value("${handbook.preview-enabled:false}") boolean previewEnabled) {
        this.catalog = catalog;
        this.previewEnabled = previewEnabled;
    }

    public record Summary(String id, String title, String summary, String category,
                          List<String> audiences, String status, LocalDate verifiedOn) {}
    public record Detail(Guide guide, String status, LocalDate verifiedOn) {}

    @GetMapping
    public List<Summary> list(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "") String audience,
            @RequestParam(defaultValue = "false") boolean preview) {
        checkPreview(preview);
        if (q.length() > 100 || (!category.isEmpty() && !GuideCatalog.CATEGORIES.contains(category))
                || (!audience.isEmpty() && !GuideCatalog.AUDIENCES.contains(audience))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "筛选条件无效");
        }
        String term = q.strip().toLowerCase(Locale.ROOT);
        return catalog.all().stream()
            .filter(g -> preview ? !g.status().equals("withdrawn") : g.effectiveStatus(LocalDate.now()).equals("published"))
            .filter(g -> category.isEmpty() || category.equals(g.category()))
            .filter(g -> audience.isEmpty() || g.audiences().contains(audience))
            .filter(g -> (g.title() + " " + g.summary() + " " + String.join(" ", g.keywords())).toLowerCase(Locale.ROOT).contains(term))
            .map(g -> new Summary(g.id(), g.title(), g.summary(), g.category(), g.audiences(), g.effectiveStatus(LocalDate.now()), g.verifiedOn()))
            .toList();
    }

    @GetMapping("/{id}")
    public Detail detail(@PathVariable String id, @RequestParam(defaultValue = "false") boolean preview) {
        checkPreview(preview);
        Guide guide = catalog.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "攻略不存在"));
        if (guide.status().equals("withdrawn")) throw new ResponseStatusException(HttpStatus.GONE, "攻略已下架");
        if (!preview && !Set.of("published", "recheck").contains(guide.status())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "攻略不存在");
        }
        return new Detail(guide, guide.effectiveStatus(LocalDate.now()), guide.verifiedOn());
    }

    private void checkPreview(boolean preview) {
        if (preview && !previewEnabled) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "预览未开启");
    }
}
