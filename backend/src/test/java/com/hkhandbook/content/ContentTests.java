package com.hkhandbook.content;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ContentTests {
    static final JsonMapper MAPPER = JsonMapper.builder().build();
    static final Clock NOW = Clock.fixed(Instant.parse("2026-10-19T08:00:00Z"), ZoneOffset.UTC);
    static ObjectNode fixture() throws Exception {
        try (var in = new ClassPathResource("content-fixtures/test-guide.json").getInputStream()) {
            return (ObjectNode) MAPPER.readTree(in);
        }
    }
    static Resource resource(String filename, String json) {
        return new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8)) {
            @Override public String getFilename() { return filename; }
        };
    }
    static Resource resource(ObjectNode a) { return resource(a.get("id").stringValue() + ".json", a.toString()); }
    static ContentCatalog catalog(ObjectNode... articles) throws Exception {
        return new ContentCatalog(Arrays.stream(articles).map(ContentTests::resource).toArray(Resource[]::new), NOW);
    }
    static ObjectNode object(ObjectNode a, String pointer) { return (ObjectNode) a.at(pointer); }
    static MockMvc mvc(ContentCatalog catalog) { return MockMvcBuilders.standaloneSetup(new ContentController(catalog)).build(); }

    @Test void validContentIsProjectedWithoutInternalEvidence() throws Exception {
        var app = mvc(catalog(fixture()));
        var body = app.perform(get("/api/guides/test-guide")).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(jsonPath("$.verifiedOn").value("2026-10-18"))
            .andExpect(jsonPath("$.sections.steps.blocks[0].text").value("Synthetic test statement"))
            .andExpect(jsonPath("$.related").isEmpty()).andReturn().getResponse().getContentAsString();
        for (String secret : List.of("test-reviewer", "test-reader", "TEST ONLY", "rolesNote", "linkCheck", "verification", "evidence")) assertFalse(body.contains(secret), secret);
    }

    @Test void hiddenDocumentsNeverAffectSearchCountsOrPaging() throws Exception {
        var draft = fixture(); draft.put("id", "draft").put("status", "draft");
        var published = fixture();
        var second = fixture(); second.put("id", "z-guide").put("category", "phone");
        var app = mvc(catalog(draft, published, second));
        app.perform(get("/api/guides").param("size", "1")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2)).andExpect(jsonPath("$.items[0].id").value("test-guide"));
        app.perform(get("/api/guides").param("size", "1").param("page", "2")).andExpect(jsonPath("$.items[0].id").value("z-guide"));
        app.perform(get("/api/guides").param("category", "life").param("audience", "visitor").param("q", "mtr")).andExpect(jsonPath("$.total").value(1));
        app.perform(get("/api/guides").param("category", "life").param("audience", "family")).andExpect(jsonPath("$.total").value(0)).andExpect(jsonPath("$.items").isEmpty());
        app.perform(get("/api/guides").param("q", "不存在")).andExpect(jsonPath("$.total").value(0));
        app.perform(get("/api/guides").param("page", "1000000")).andExpect(jsonPath("$.items").isEmpty());
        app.perform(get("/api/guides/draft")).andExpect(status().isNotFound()).andExpect(jsonPath("$.title").doesNotExist());
    }

    @Test void searchRanksPublicTextAndReturnsBodyExcerptBeforePaging() throws Exception {
        var body = fixture(); body.put("id", "a-body");
        object(body, "/sections/steps/blocks/0").put("text", "前文".repeat(40) + "电话卡办理" + "后文".repeat(100));
        var title = fixture(); title.put("id", "z-title").put("title", "电话卡办理指南");
        var keyword = fixture(); keyword.put("id", "b-keyword").putArray("keywords").add("电话卡");
        var hidden = fixture(); hidden.put("id", "hidden").put("status", "draft").put("title", "电话卡");
        var app = mvc(catalog(body, title, keyword, hidden));
        app.perform(get("/api/guides").param("q", "电话卡").param("size", "1"))
            .andExpect(jsonPath("$.total").value(3)).andExpect(jsonPath("$.items[0].id").value("z-title"));
        app.perform(get("/api/guides").param("q", "电话卡").param("size", "1").param("page", "2"))
            .andExpect(jsonPath("$.items[0].id").value("b-keyword"));
        app.perform(get("/api/guides").param("q", "电话卡").param("size", "1").param("page", "3"))
            .andExpect(jsonPath("$.items[0].id").value("a-body"))
            .andExpect(jsonPath("$.items[0].matchExcerpt").value(org.hamcrest.Matchers.containsString("电话卡办理")));
        app.perform(get("/api/guides").param("q", "电话卡").param("category", "bank"))
            .andExpect(jsonPath("$.total").value(0));
        app.perform(get("/api/guides").param("q", "test-reviewer")).andExpect(jsonPath("$.total").value(0));
        app.perform(get("/api/guides")).andExpect(jsonPath("$.items[0].matchExcerpt").doesNotExist());
    }

    @Test void recommendationsOnlyReorderVisibleUnqueriedResults() throws Exception {
        var a = fixture(); a.put("id", "a-guide").put("title", "独有标题");
        var z = fixture(); z.put("id", "z-guide");
        var draft = fixture(); draft.put("id", "draft").put("status", "draft");
        var app = mvc(new ContentCatalog(new Resource[]{resource(a), resource(z), resource(draft)}, NOW,
            List.of("draft", "missing", "z-guide")));
        app.perform(get("/api/guides")).andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.items[0].id").value("z-guide"));
        app.perform(get("/api/guides").param("q", "独有标题"))
            .andExpect(jsonPath("$.items[0].id").value("a-guide"));
    }

    static Stream<Consumer<ObjectNode>> blockedDocuments() {
        return Stream.of(
            a -> a.put("status", "draft"), a -> a.put("status", "pending_review"), a -> a.put("status", "review_due"),
            a -> a.putNull("review"), a -> a.putNull("publication"), a -> a.putNull("reviewDueOn"),
            a -> a.put("reviewDueOn", "2026-10-19"),
            a -> object(a,"/facts/0").putNull("verification"),
            a -> object(a,"/facts/0/verification").put("result", "conflict"),
            a -> object(a,"/facts/0/verification").put("revision", 2),
            a -> object(a,"/facts/0/verification").put("reviewDueOn", "2026-10-19"),
            a -> object(a,"/sources/0").putNull("linkCheck"),
            a -> object(a,"/sources/0/linkCheck").put("result", "broken"),
            a -> object(a,"/sources/0/linkCheck").put("reviewDueOn", "2026-10-19"),
            a -> object(a,"/review").put("result", "changes_requested"),
            a -> object(a,"/review").put("revision", 2),
            a -> object(a,"/publication").put("revision", 2),
            a -> object(a,"/publication").put("publishedOn", "2026-10-17"),
            a -> object(a,"/effectivePeriod").put("state", "unknown"),
            a -> object(a,"/effectivePeriod").put("state", "known").put("from", "2026-10-20"),
            a -> object(a,"/effectivePeriod").put("state", "known").put("until", "2026-10-19"),
            a -> object(a,"/sources/0/effectivePeriod").put("state", "unknown"),
            a -> object(a,"/sources/0/effectivePeriod").put("state", "known").put("until", "2026-10-20"),
            a -> { var s = object(a,"/sections/steps"); s.put("state", "unknown").put("reason", "Unknown test step"); s.putArray("blocks"); },
            a -> object(a,"/review").put("reviewedOn", "2026-10-17")
        );
    }
    @ParameterizedTest @MethodSource("blockedDocuments")
    void gatesApplyToListAndDetail(Consumer<ObjectNode> mutation) throws Exception {
        var a = fixture(); mutation.accept(a); var app = mvc(catalog(a));
        app.perform(get("/api/guides")).andExpect(jsonPath("$.total").value(0));
        app.perform(get("/api/guides/test-guide")).andExpect(status().isNotFound()).andExpect(jsonPath("$.sections").doesNotExist());
    }

    static Stream<Consumer<ObjectNode>> malformedDocuments() {
        return Stream.of(
            a -> a.put("unexpected", true), a -> a.remove("title"), a -> a.put("title", " "),
            a -> a.put("schemaVersion", 2), a -> a.put("revision", 1.5), a -> a.put("category", "transport"),
            a -> a.put("editedOn", "2026-02-30"), a -> a.put("editedOn", "2026-10-20"),
            a -> a.put("title", "<script>alert(1)</script>"),
            a -> object(a,"/facts/0").put("id", "unreferenced"),
            a -> object(a,"/facts/0").putArray("sourceIds").add("missing"),
            a -> object(a,"/sources/0").put("url", "javascript:alert(1)"),
            a -> object(a,"/sources/0").put("url", "https://user@example.org/path"),
            a -> object(a,"/sources/0").putNull("consultedOn"),
            a -> object(a,"/review").put("human", "AI"),
            a -> object(a,"/review").put("reviewedOn", "2026-10-20"),
            a -> object(a,"/effectivePeriod").put("state", "known").put("from", "2026-10-19").put("until", "2026-10-18"),
            a -> a.putArray("audiences").add("visitor").add("visitor")
        );
    }
    @ParameterizedTest @MethodSource("malformedDocuments")
    void invalidStructureRejectsEntireDataset(Consumer<ObjectNode> mutation) throws Exception {
        var a = fixture(); a.put("id", "invalid-guide"); mutation.accept(a);
        var error = assertThrows(IllegalStateException.class, () -> catalog(fixture(), a));
        assertTrue(error.getMessage().contains("invalid-guide.json"));
    }

    @Test void parserRejectsDuplicateKeysTrailingDataFilenamesAndIds() throws Exception {
        String json = fixture().toString();
        for (var r : List.of(resource("test-guide.json", json.replace("\"title\":", "\"title\":\"duplicate\",\"title\":")), resource("test-guide.json", json + " {}"), resource("wrong.json", json))) {
            assertThrows(IllegalStateException.class, () -> new ContentCatalog(new Resource[]{r}, NOW));
        }
        assertThrows(IllegalStateException.class, () -> catalog(fixture(), fixture()));
    }

    @Test void queryErrorsAndWithdrawalHaveDistinctSafeResponses() throws Exception {
        var a = fixture(); a.put("status", "withdrawn"); var app = mvc(catalog(a));
        app.perform(get("/api/guides/test-guide")).andExpect(status().isGone()).andExpect(jsonPath("$.id").value("test-guide")).andExpect(jsonPath("$.title").doesNotExist());
        app.perform(get("/api/guides/missing")).andExpect(status().isNotFound());
        for (var pair : List.of(new String[]{"preview", "true"}, new String[]{"category", "bad"}, new String[]{"audience", "bad"}, new String[]{"size", "51"}, new String[]{"page", "0"}, new String[]{"page", "999999999999"}, new String[]{"q", "字".repeat(101)})) {
            app.perform(get("/api/guides").param(pair[0], pair[1])).andExpect(status().isBadRequest());
        }
        app.perform(get("/api/guides").param("category", "life", "bank")).andExpect(status().isBadRequest());
        app.perform(get("/api/guides/test-guide").param("preview", "true")).andExpect(status().isBadRequest());
        app.perform(get("/api/guides")).andExpect(jsonPath("$.items").isEmpty());
    }

    @Test void emptyProductionCatalogAndTrimmedUnicodeInputAreSupported() throws Exception {
        mvc(new ContentCatalog(new Resource[0], NOW)).perform(get("/api/guides"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        var a = fixture(); a.put("title", "  " + "港".repeat(120) + "  ");
        assertEquals("港".repeat(120), catalog(a).find("test-guide").get("title").stringValue());
        a.put("title", "港".repeat(121));
        assertThrows(IllegalStateException.class, () -> catalog(a));
    }

    @Test void inclusiveStartDateAndNotApplicableSectionsRemainPublic() throws Exception {
        var a = fixture();
        object(a, "/effectivePeriod").put("state", "known").put("from", "2026-10-19").put("until", "2026-10-21");
        var materials = object(a, "/sections/materials");
        materials.put("state", "not_applicable").put("reason", "Synthetic query tutorial needs no application material");
        materials.putArray("blocks");
        assertEquals(1, catalog(a).visible().size());
    }

    @Test void hongKongMidnightExpiresContentWithoutReload() throws Exception {
        var a = fixture(); a.put("reviewDueOn", "2026-10-20");
        var clock = new MutableClock(Instant.parse("2026-10-19T15:59:59Z"));
        var catalog = new ContentCatalog(new Resource[]{resource(a)}, clock);
        assertEquals(1, catalog.visible().size());
        clock.instant = Instant.parse("2026-10-19T16:00:00Z");
        assertTrue(catalog.visible().isEmpty());
        assertFalse(catalog.visible(catalog.find("test-guide")));
    }
    static final class MutableClock extends Clock {
        Instant instant; ZoneId zone = ZoneOffset.UTC;
        MutableClock(Instant instant) { this.instant = instant; }
        public ZoneId getZone() { return zone; }
        public Clock withZone(ZoneId zone) { return Clock.fixed(instant, zone); }
        public Instant instant() { return instant; }
    }
}
