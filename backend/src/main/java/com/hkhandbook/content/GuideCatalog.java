package com.hkhandbook.content;

import java.time.LocalDate;
import java.util.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class GuideCatalog {
    public static final Set<String> CATEGORIES = Set.of("bank", "phone", "transport");
    public static final Set<String> AUDIENCES = Set.of("visitor", "talent", "family");
    private final Map<String, Guide> guides = new LinkedHashMap<>();

    public GuideCatalog(JsonMapper mapper) throws Exception {
        for (var resource : new PathMatchingResourcePatternResolver().getResources("classpath*:guides/*.json")) {
            Guide guide;
            try (var stream = resource.getInputStream()) { guide = mapper.readValue(stream, Guide.class); }
            validate(guide, LocalDate.now());
            if (guides.putIfAbsent(guide.id(), guide) != null) throw new IllegalArgumentException("Duplicate guide ID: " + guide.id());
        }
    }

    public Collection<Guide> all() { return Collections.unmodifiableCollection(guides.values()); }
    public Optional<Guide> find(String id) { return Optional.ofNullable(guides.get(id)); }

    public static void validate(Guide g, LocalDate today) {
        require(g.id() != null && g.id().matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "stable ID required");
        require(g.version() > 0 && present(g.title()) && present(g.summary()), "title, summary and version required");
        require(g.category() != null && CATEGORIES.contains(g.category()), "unknown category");
        require(g.audiences() != null && !g.audiences().isEmpty() && g.audiences().stream().allMatch(a -> a != null && AUDIENCES.contains(a)), "unknown audience");
        require(g.keywords() != null && !g.keywords().isEmpty() && g.keywords().stream().allMatch(GuideCatalog::present), "keywords required");
        require(g.editedOn() != null && !g.editedOn().isAfter(today), "actual edit date required");
        require(g.status() != null && Set.of("draft", "unverified", "review", "published", "recheck", "withdrawn").contains(g.status()), "unknown status");
        require(g.sections() != null && !g.sections().isEmpty() && g.sources() != null && !g.sources().isEmpty(), "body and sources required");
        Set<String> ids = new HashSet<>();
        for (var s : g.sources()) {
            require(present(s.id()) && ids.add(s.id()) && present(s.title()), "unique source ID and title required");
            require(s.url() != null && s.url().matches("https://[^\\s]+"), "HTTPS source URL required");
            require(s.accessedOn() != null && !s.accessedOn().isAfter(today), "actual source access date required");
            require((s.verifiedOn() == null && !present(s.verifiedBy())) || (s.verifiedOn() != null && !s.verifiedOn().isAfter(today) && present(s.verifiedBy())), "verification date and person must match");
        }
        for (var section : g.sections()) {
            require(present(section.heading()) && present(section.text()), "section heading and text required");
            require(section.kind() != null && Set.of("official", "editorial", "experience", "unverified").contains(section.kind()), "unknown information kind");
            require(section.sourceIds() != null && ids.containsAll(section.sourceIds()), "unknown source reference");
            require(!section.kind().equals("official") || !section.sourceIds().isEmpty(), "official facts need sources");
        }
        boolean publicContent = Set.of("published", "recheck").contains(g.status());
        if (publicContent) {
            require(g.verifiedOn() != null && g.sources().stream().allMatch(s -> present(s.verifiedBy())), "human verification required");
            require(present(g.approvedBy()) && present(g.publishedBy()), "approval and publisher required");
            require(g.publishedOn() != null && !g.publishedOn().isAfter(today), "actual publication date required");
            require(g.sources().stream().noneMatch(s -> s.verifiedOn().isAfter(g.publishedOn())), "verify before publication");
            require(g.reviewDueOn() != null && g.reviewDueOn().isAfter(g.verifiedOn()), "review deadline required");
            require(g.sections().stream().noneMatch(s -> s.kind().equals("unverified")), "unverified facts block publication");
            for (String heading : List.of("适用条件", "准备事项", "操作步骤", "费用与耗时", "注意事项")) {
                require(g.sections().stream().anyMatch(s -> s.heading().equals(heading)), "missing section: " + heading);
            }
        }
        if (g.status().equals("withdrawn")) require(present(g.withdrawalReason()), "withdrawal reason required");
    }

    private static boolean present(String s) { return s != null && !s.isBlank(); }
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalArgumentException(message); }
}
