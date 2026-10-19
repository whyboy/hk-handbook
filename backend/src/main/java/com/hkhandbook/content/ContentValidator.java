package com.hkhandbook.content;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/** The v1 editorial contract. Structural errors reject the entire dataset. */
final class ContentValidator {
    static final Set<String> CATEGORIES = Set.of("bank", "phone", "talent", "life");
    static final Set<String> AUDIENCES = Set.of("visitor", "talent", "family");
    static final List<String> SECTIONS = List.of("applicability", "materials", "steps", "costsAndTime", "warnings");
    private static final Pattern ID = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private final LocalDate today;

    ContentValidator(LocalDate today) { this.today = today; }

    void validate(JsonNode a) {
        fields(a, "article", "schemaVersion id revision status title summary category audiences keywords editedOn effectivePeriod reviewDueOn sections facts sources review publication related");
        positive(a.get("schemaVersion"), "schemaVersion");
        require(a.get("schemaVersion").intValue() == 1, "schemaVersion", "unsupported version");
        id(a.get("id"), "id"); positive(a.get("revision"), "revision");
        choice(a.get("status"), "status", Set.of("draft", "pending_review", "published", "review_due", "withdrawn"));
        text(a.get("title"), "title", 120); text(a.get("summary"), "summary", 500);
        choice(a.get("category"), "category", CATEGORIES);
        var audiences = strings(a.get("audiences"), "audiences", 1, 3, 40);
        require(AUDIENCES.containsAll(audiences), "audiences", "unsupported audience");
        strings(a.get("keywords"), "keywords", 1, 20, 40);
        past(a.get("editedOn"), "editedOn"); period(a.get("effectivePeriod"), "effectivePeriod");
        optionalDate(a.get("reviewDueOn"), "reviewDueOn");
        fields(a.get("sections"), "sections", String.join(" ", SECTIONS));
        var usedFacts = new HashSet<String>();
        for (String key : SECTIONS) {
            String p = "sections." + key; var section = a.get("sections").get(key);
            fields(section, p, "state reason blocks");
            choice(section.get("state"), p + ".state", Set.of("known", "not_applicable", "unknown"));
            array(section.get("blocks"), p + ".blocks", 0, 100);
            if (value(section, "state").equals("known")) {
                require(section.get("reason").isNull() && !section.get("blocks").isEmpty(), p, "known section needs blocks and null reason");
            } else {
                text(section.get("reason"), p + ".reason", 1000);
                require(section.get("blocks").isEmpty(), p, "unknown/not_applicable must have no blocks");
            }
            int index = 0;
            for (var b : section.get("blocks")) {
                String bp = p + ".blocks[" + index++ + "]";
                fields(b, bp, "kind text factIds experienceContext");
                choice(b.get("kind"), bp + ".kind", Set.of("official_fact", "editorial_advice", "personal_experience"));
                text(b.get("text"), bp + ".text", 5000);
                var refs = strings(b.get("factIds"), bp + ".factIds", value(b, "kind").equals("official_fact") ? 1 : 0, 200, 80);
                refs.forEach(ref -> require(ID.matcher(ref).matches(), bp, "invalid fact ID"));
                usedFacts.addAll(refs);
                if (value(b, "kind").equals("personal_experience")) text(b.get("experienceContext"), bp + ".experienceContext", 1000);
                else require(b.get("experienceContext").isNull(), bp, "non-experience block must have null context");
            }
        }
        array(a.get("facts"), "facts", 0, 200); array(a.get("sources"), "sources", 0, 100);
        var factIds = new HashSet<String>(); var sourceIds = new HashSet<String>(); var usedSources = new HashSet<String>();
        int index = 0;
        for (var f : a.get("facts")) {
            String p = "facts[" + index++ + "]";
            fields(f, p, "id claim conditions sourceIds verification");
            require(factIds.add(id(f.get("id"), p + ".id")), p, "duplicate ID");
            text(f.get("claim"), p + ".claim", 2000); text(f.get("conditions"), p + ".conditions", 2000);
            usedSources.addAll(strings(f.get("sourceIds"), p + ".sourceIds", 1, 100, 80));
            if (!f.get("verification").isNull()) signature(f.get("verification"), p + ".verification", "verification");
        }
        require(factIds.equals(usedFacts), "facts", "unused or unresolved fact references");
        index = 0;
        for (var s : a.get("sources")) {
            String p = "sources[" + index++ + "]";
            fields(s, p, "id organization title url locator scope consultedOn consultedBy effectivePeriod linkCheck");
            require(sourceIds.add(id(s.get("id"), p + ".id")), p, "duplicate ID");
            text(s.get("organization"), p + ".organization", 200); text(s.get("title"), p + ".title", 200);
            String url = text(s.get("url"), p + ".url", 2048);
            try {
                URI uri = URI.create(url);
                require("https".equals(uri.getScheme()) && uri.getHost() != null && uri.getRawUserInfo() == null, p + ".url", "absolute HTTPS URL without credentials required");
            } catch (IllegalArgumentException e) { throw new IllegalArgumentException(p + ".url: invalid HTTPS URL"); }
            text(s.get("locator"), p + ".locator", 2000); text(s.get("scope"), p + ".scope", 2000);
            require(s.get("consultedOn").isNull() == s.get("consultedBy").isNull(), p, "consultation date and author must be paired");
            if (!s.get("consultedOn").isNull()) { past(s.get("consultedOn"), p + ".consultedOn"); text(s.get("consultedBy"), p + ".consultedBy", 120); }
            period(s.get("effectivePeriod"), p + ".effectivePeriod");
            if (!s.get("linkCheck").isNull()) signature(s.get("linkCheck"), p + ".linkCheck", "linkCheck");
        }
        require(sourceIds.containsAll(usedSources), "sources", "unresolved source references");
        if (!a.get("review").isNull()) signature(a.get("review"), "review", "review");
        if (!a.get("publication").isNull()) signature(a.get("publication"), "publication", "publication");
        array(a.get("related"), "related", 0, 20);
        var relatedIds = new HashSet<String>();
        for (var r : a.get("related")) {
            fields(r, "related", "type id version"); choice(r.get("type"), "related.type", Set.of("bank", "talent_rule", "checklist"));
            id(r.get("id"), "related.id"); positive(r.get("version"), "related.version");
            require(relatedIds.add(value(r, "type") + ":" + value(r, "id")), "related", "duplicate reference");
        }
    }

    private void signature(JsonNode s, String p, String type) {
        String extras = switch (type) {
            case "verification" -> "verifiedOn result revision reviewDueOn";
            case "linkCheck" -> "checkedOn result reviewDueOn";
            case "review" -> "reviewedOn result revision rolesNote";
            default -> "publishedOn revision";
        };
        fields(s, p, "human evidence " + extras);
        String human = text(s.get("human"), p + ".human", 120);
        require(!Set.of("ai", "chatgpt", "codex", "assistant").contains(human.toLowerCase(Locale.ROOT)), p + ".human", "human attribution required");
        text(s.get("evidence"), p + ".evidence", 2000);
        if (s.has("revision")) positive(s.get("revision"), p + ".revision");
        if (s.has("reviewDueOn")) date(s.get("reviewDueOn"), p + ".reviewDueOn");
        switch (type) {
            case "verification" -> { past(s.get("verifiedOn"), p + ".verifiedOn"); choice(s.get("result"), p + ".result", Set.of("passed", "conflict", "pending")); }
            case "linkCheck" -> { past(s.get("checkedOn"), p + ".checkedOn"); choice(s.get("result"), p + ".result", Set.of("valid", "broken", "unknown")); }
            case "review" -> { past(s.get("reviewedOn"), p + ".reviewedOn"); choice(s.get("result"), p + ".result", Set.of("approved", "changes_requested")); text(s.get("rolesNote"), p + ".rolesNote", 1000); }
            default -> past(s.get("publishedOn"), p + ".publishedOn");
        }
    }

    private void period(JsonNode p, String path) {
        fields(p, path, "state from until note"); choice(p.get("state"), path + ".state", Set.of("known", "not_stated", "unknown"));
        text(p.get("note"), path + ".note", 1000);
        LocalDate from = optionalDate(p.get("from"), path + ".from"), until = optionalDate(p.get("until"), path + ".until");
        require(value(p, "state").equals("known") ? from != null || until != null : from == null && until == null, path, "dates inconsistent with state");
        require(from == null || until == null || from.isBefore(until), path, "from must precede until");
    }

    static String value(JsonNode n, String field) { return n.get(field).stringValue(); }
    static void require(boolean ok, String path, String message) { if (!ok) throw new IllegalArgumentException(path + ": " + message); }
    private static void fields(JsonNode n, String path, String names) {
        require(n != null && n.isObject(), path, "object required");
        require(new HashSet<>(n.propertyNames()).equals(Set.of(names.split(" "))), path, "missing or unknown fields");
    }
    private static String text(JsonNode n, String path, int max) {
        require(n != null && n.isString(), path, "text required");
        String s = n.stringValue();
        require(!s.isBlank() && s.codePointCount(0, s.length()) <= max, path, "text length out of range");
        require(!Pattern.compile("<\\s*/?\\s*[A-Za-z!][^>]*>").matcher(s).find(), path, "plain text required");
        return s;
    }
    private static String id(JsonNode n, String path) {
        String s = text(n, path, 80); require(ID.matcher(s).matches(), path, "invalid ID"); return s;
    }
    private static void positive(JsonNode n, String path) { require(n != null && n.isIntegralNumber() && n.canConvertToInt() && n.intValue() > 0, path, "positive integer required"); }
    private static void choice(JsonNode n, String path, Set<String> options) { require(options.contains(text(n, path, 80)), path, "unsupported value"); }
    private static void array(JsonNode n, String path, int min, int max) { require(n != null && n.isArray() && n.size() >= min && n.size() <= max, path, "array size/type invalid"); }
    private static Set<String> strings(JsonNode n, String path, int min, int max, int length) {
        array(n, path, min, max); var values = new HashSet<String>();
        for (var item : n) require(values.add(text(item, path, length)), path, "duplicate value");
        return values;
    }
    static LocalDate date(JsonNode n, String path) {
        String s = text(n, path, 10);
        require(s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"), path, "ISO date required");
        try { return LocalDate.parse(s); } catch (java.time.DateTimeException e) { throw new IllegalArgumentException(path + ": invalid calendar date"); }
    }
    private static LocalDate optionalDate(JsonNode n, String path) { return n.isNull() ? null : date(n, path); }
    private void past(JsonNode n, String path) { require(!date(n, path).isAfter(today), path, "future actual date"); }
}
