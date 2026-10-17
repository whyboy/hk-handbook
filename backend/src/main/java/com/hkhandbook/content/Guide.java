package com.hkhandbook.content;

import java.time.LocalDate;
import java.util.List;

/** Manually edited content. Dates record actual work, never inferred from build time. */
public record Guide(String id, int version, String status, String title, String summary,
                    String category, List<String> audiences, List<String> keywords,
                    LocalDate editedOn, LocalDate publishedOn, LocalDate reviewDueOn,
                    String approvedBy, String publishedBy, String withdrawalReason,
                    List<Section> sections, List<Source> sources) {
    public record Section(String heading, String text, String kind, List<String> sourceIds) {}
    public record Source(String id, String title, String url, LocalDate accessedOn,
                         LocalDate verifiedOn, String verifiedBy) {}

    public LocalDate verifiedOn() {
        if (sources.isEmpty() || sources.stream().anyMatch(s -> s.verifiedOn() == null)) return null;
        return sources.stream().map(Source::verifiedOn).min(LocalDate::compareTo).orElse(null);
    }

    public String effectiveStatus(LocalDate today) {
        if (status.equals("published") && !reviewDueOn.isAfter(today)) return "recheck";
        return status;
    }
}
