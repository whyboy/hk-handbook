package com.hkhandbook.content;

import java.io.IOException;
import java.time.*;
import java.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.*;
import static com.hkhandbook.content.ContentValidator.*;

@Service
public class ContentCatalog {
    private final Map<String, JsonNode> articles;
    private final Clock clock;
    private static final ZoneId HONG_KONG = ZoneId.of("Asia/Hong_Kong");
    private static final JsonMapper MAPPER = JsonMapper.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();

    public ContentCatalog() throws IOException {
        this(new PathMatchingResourcePatternResolver().getResources("classpath*:content/*.json"), Clock.systemUTC());
    }

    ContentCatalog(Resource[] resources, Clock clock) throws IOException {
        this.clock = clock;
        var loaded = new LinkedHashMap<String, JsonNode>();
        var validator = new ContentValidator(today());
        for (var resource : resources) {
            try (var input = resource.getInputStream()) {
                JsonNode article = normalize(MAPPER.readTree(input));
                validator.validate(article);
                String id = value(article, "id");
                require((id + ".json").equals(resource.getFilename()), "filename", "must match article ID");
                require(loaded.putIfAbsent(id, article) == null, "id", "duplicate article ID");
                var blocked = PublicationPolicy.blockers(article, today());
                if (!blocked.isEmpty()) LoggerFactory.getLogger(ContentCatalog.class).info("Content {} is not public: {}", id, blocked);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Invalid content file " + resource.getFilename() + ": " + e.getMessage(), e);
            }
        }
        articles = Collections.unmodifiableMap(loaded);
    }

    LocalDate today() { return LocalDate.now(clock.withZone(HONG_KONG)); }
    List<JsonNode> visible() {
        LocalDate now = today();
        return articles.values().stream().filter(a -> PublicationPolicy.blockers(a, now).isEmpty())
            .sorted(Comparator.<JsonNode, String>comparing(a -> value(a.get("publication"), "publishedOn")).reversed().thenComparing(a -> value(a, "id"))).toList();
    }
    JsonNode find(String id) { return articles.get(id); }
    boolean visible(JsonNode a) { return PublicationPolicy.blockers(a, today()).isEmpty(); }

    /** Explicit allowlist: internal identities, notes and evidence never enter the response. */
    static ObjectNode summary(JsonNode a) {
        ObjectNode out = pick(a, "id", "revision", "title", "summary", "category", "audiences", "keywords", "editedOn", "reviewDueOn");
        out.set("effectivePeriod", pick(a.get("effectivePeriod"), "state", "from", "until"));
        out.set("publishedOn", a.get("publication").get("publishedOn"));
        String verified = null;
        for (var f : a.get("facts")) {
            String date = value(f.get("verification"), "verifiedOn");
            if (verified == null || date.compareTo(verified) < 0) verified = date;
        }
        out.put("verifiedOn", verified);
        return out;
    }
    static ObjectNode detail(JsonNode a) {
        ObjectNode out = summary(a), sections = MAPPER.createObjectNode();
        for (String name : SECTIONS) {
            var section = a.get("sections").get(name);
            ObjectNode item = pick(section, "state", "reason");
            ArrayNode blocks = MAPPER.createArrayNode();
            for (var b : section.get("blocks")) blocks.add(pick(b, "kind", "text", "factIds", "experienceContext"));
            item.set("blocks", blocks); sections.set(name, item);
        }
        out.set("sections", sections);
        ArrayNode facts = MAPPER.createArrayNode(), sources = MAPPER.createArrayNode();
        for (var f : a.get("facts")) facts.add(pick(f, "id", "claim", "conditions", "sourceIds"));
        for (var s : a.get("sources")) {
            ObjectNode source = pick(s, "id", "organization", "title", "url", "consultedOn");
            source.set("effectivePeriod", pick(s.get("effectivePeriod"), "state", "from", "until"));
            sources.add(source);
        }
        out.set("facts", facts); out.set("sources", sources);
        // No bank/rule/checklist resolver exists yet: unresolved targets never become actions.
        out.set("related", MAPPER.createArrayNode());
        return out;
    }
    private static ObjectNode pick(JsonNode source, String... fields) {
        ObjectNode result = MAPPER.createObjectNode();
        for (String field : fields) result.set(field, source.get(field).deepCopy());
        return result;
    }
    private static JsonNode normalize(JsonNode n) {
        if (n.isString()) return StringNode.valueOf(n.stringValue().strip());
        if (n.isObject()) {
            ObjectNode out = MAPPER.createObjectNode();
            for (String key : n.propertyNames()) out.set(key, normalize(n.get(key)));
            return out;
        }
        if (n.isArray()) { ArrayNode out = MAPPER.createArrayNode(); n.forEach(item -> out.add(normalize(item))); return out; }
        return n;
    }
}
