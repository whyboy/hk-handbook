package com.hkhandbook.content;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class GuideTests {
    @Autowired GuideCatalog catalog;
    @Autowired GuideController publicApi;
    @Autowired JdbcTemplate jdbc;

    @Test void sampleIsNotAccidentallyPublished() {
        assertEquals(List.of(), publicApi.list("", "", "", false));
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> publicApi.detail("mtr-route", false)).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> publicApi.list("", "", "", true));
        var sample = catalog.find("mtr-route").orElseThrow();
        assertNull(sample.verifiedOn());
        assertNull(sample.publishedOn());
        assertEquals("unverified", sample.status());
    }

    @Test void previewSupportsCombinedSearchAndFilters() {
        var api = new GuideController(catalog, true);
        assertEquals(1, api.list("MTR", "transport", "family", true).size());
        assertEquals(0, api.list("港铁", "bank", "family", true).size());
        assertEquals(0, api.list("不存在", "", "", true).size());
        assertThrows(ResponseStatusException.class, () -> api.list("", "bad", "", true));
        assertEquals(4, api.detail("mtr-route", true).guide().sources().size());
    }

    @Test void publicationRequiresHumanVerificationAndApproval() {
        var valid = published("published", LocalDate.now().plusDays(10));
        assertDoesNotThrow(() -> GuideCatalog.validate(valid, LocalDate.now()));
        var sample = catalog.find("mtr-route").orElseThrow();
        var invalid = new Guide(sample.id(), 1, "published", sample.title(), sample.summary(), sample.category(),
            sample.audiences(), sample.keywords(), sample.editedOn(), LocalDate.now(), LocalDate.now().plusDays(90),
            "", "", null, sample.sections(), sample.sources());
        assertThrows(IllegalArgumentException.class, () -> GuideCatalog.validate(invalid, LocalDate.now()));
        var missingSources = new Guide(valid.id(), 1, valid.status(), valid.title(), valid.summary(), valid.category(),
            valid.audiences(), valid.keywords(), valid.editedOn(), valid.publishedOn(), valid.reviewDueOn(), "reviewer", "publisher", null,
            List.of(new Guide.Section("操作步骤", "fact", "official", List.of("missing"))), valid.sources());
        assertThrows(IllegalArgumentException.class, () -> GuideCatalog.validate(missingSources, LocalDate.now()));
    }

    @Test void expiredAndWithdrawnGuidesLeaveSearchButKeepStableStatus() {
        var stale = published("published", LocalDate.now());
        var withdrawn = published("withdrawn", LocalDate.now().plusDays(1));
        var fake = mock(GuideCatalog.class);
        when(fake.all()).thenReturn(List.of(stale));
        when(fake.find("fixture")).thenReturn(Optional.of(stale));
        var api = new GuideController(fake, false);
        assertTrue(api.list("", "", "", false).isEmpty());
        assertEquals("recheck", api.detail("fixture", false).status());
        when(fake.find("fixture")).thenReturn(Optional.of(withdrawn));
        assertEquals(410, assertThrows(ResponseStatusException.class, () -> api.detail("fixture", false)).getStatusCode().value());
    }

    @Test void correctionIsPersistedAndInvalidOrPrivateSubmissionsAreRejected() {
        var previewApi = new GuideController(catalog, true);
        var controller = new CorrectionController(jdbc, previewApi);
        var result = controller.submit("mtr-route", new CorrectionController.Submission("other", "需要进一步核实", ""), true);
        assertEquals("需要进一步核实", jdbc.queryForObject("SELECT message FROM corrections WHERE id = ?", String.class, result.get("id")));
        assertEquals(1, jdbc.queryForObject("SELECT preview FROM corrections WHERE id = ?", Integer.class, result.get("id")));
        assertThrows(ResponseStatusException.class, () -> controller.submit("mtr-route", new CorrectionController.Submission("other", " ", ""), true));
        assertThrows(ResponseStatusException.class, () -> controller.submit("mtr-route", new CorrectionController.Submission("other", "a".repeat(2001), ""), true));
        assertThrows(ResponseStatusException.class, () -> controller.submit("mtr-route", new CorrectionController.Submission("other", "test", "javascript:alert(1)"), true));
        assertThrows(ResponseStatusException.class, () -> controller.submit("mtr-route", new CorrectionController.Submission("other", "test", ""), false));
    }

    private Guide published(String status, LocalDate due) {
        var day = LocalDate.now().minusDays(1);
        var sections = List.of("适用条件", "准备事项", "操作步骤", "费用与耗时", "注意事项").stream()
            .map(h -> new Guide.Section(h, "仅用于测试的内容", "official", List.of("S1"))).toList();
        return new Guide("fixture", 1, status, "测试", "测试摘要", "transport", List.of("visitor"), List.of("test"),
            day, day, due, "test-reviewer", "test-publisher", "测试下架", sections,
            List.of(new Guide.Source("S1", "test source", "https://example.com/", day, day, "test-verifier")));
    }
}
