package com.hkhandbook.content;

import java.time.LocalDate;
import java.util.*;
import tools.jackson.databind.JsonNode;
import static com.hkhandbook.content.ContentValidator.*;

/** Evaluated for every request using the Hong Kong calendar, including midnight expiry. */
final class PublicationPolicy {
    static List<String> blockers(JsonNode a, LocalDate today) {
        var reasons = new ArrayList<String>();
        if (!value(a, "status").equals("published")) reasons.add("status");
        if (!active(a.get("effectivePeriod"), today)) reasons.add("effectivePeriod");
        if (!future(a.get("reviewDueOn"), today)) reasons.add("reviewDueOn");
        if (!withinUntil(a.get("reviewDueOn"), a.get("effectivePeriod"))) reasons.add("reviewDueOn exceeds validity");
        for (String name : SECTIONS) {
            String state = value(a.get("sections").get(name), "state");
            if (state.equals("unknown") || (Set.of("steps", "applicability").contains(name) && !state.equals("known"))) reasons.add("sections." + name);
        }
        var review = a.get("review"); var publication = a.get("publication");
        int revision = a.get("revision").intValue();
        if (review.isNull() || !value(review, "result").equals("approved") || review.get("revision").intValue() != revision) reasons.add("review");
        if (publication.isNull() || publication.get("revision").intValue() != revision) reasons.add("publication");
        if (!review.isNull()) {
            if (!ordered(a.get("editedOn"), review.get("reviewedOn"))) reasons.add("review before edit");
            if (!publication.isNull() && !ordered(review.get("reviewedOn"), publication.get("publishedOn"))) reasons.add("publication before review");
        }
        if (a.get("facts").isEmpty() || a.get("sources").isEmpty()) reasons.add("facts/sources required");
        Set<String> used = new HashSet<>();
        for (var f : a.get("facts")) {
            f.get("sourceIds").forEach(s -> used.add(s.stringValue()));
            var v = f.get("verification"); String p = "facts." + value(f, "id");
            if (v.isNull()) { reasons.add(p); continue; }
            if (!value(v, "result").equals("passed") || v.get("revision").intValue() != revision || !future(v.get("reviewDueOn"), today)) reasons.add(p);
            if (!ordered(a.get("editedOn"), v.get("verifiedOn"))) reasons.add(p + " before edit");
            if (!review.isNull() && !ordered(v.get("verifiedOn"), review.get("reviewedOn"))) reasons.add(p + " after review");
            for (var sid : f.get("sourceIds")) {
                for (var source : a.get("sources")) {
                    if (value(source, "id").equals(sid.stringValue()) && !withinUntil(v.get("reviewDueOn"), source.get("effectivePeriod"))) reasons.add(p + " exceeds source validity");
                }
            }
        }
        for (var s : a.get("sources")) {
            String p = "sources." + value(s, "id"); var check = s.get("linkCheck");
            if (!used.contains(value(s, "id")) || s.get("consultedOn").isNull() || !active(s.get("effectivePeriod"), today)) reasons.add(p);
            if (!withinUntil(a.get("reviewDueOn"), s.get("effectivePeriod"))) reasons.add(p + " article review exceeds validity");
            if (check.isNull()) { reasons.add(p + ".linkCheck"); continue; }
            if (!value(check, "result").equals("valid") || !future(check.get("reviewDueOn"), today) || !withinUntil(check.get("reviewDueOn"), s.get("effectivePeriod"))) reasons.add(p + ".linkCheck");
            if (!ordered(a.get("editedOn"), check.get("checkedOn"))) reasons.add(p + " check before edit");
            if (!review.isNull() && (!ordered(check.get("checkedOn"), review.get("reviewedOn")) || (!s.get("consultedOn").isNull() && !ordered(s.get("consultedOn"), review.get("reviewedOn"))))) reasons.add(p + " after review");
        }
        return List.copyOf(reasons);
    }
    private static boolean ordered(JsonNode first, JsonNode last) { return !date(first, "date").isAfter(date(last, "date")); }
    private static boolean future(JsonNode n, LocalDate today) { return !n.isNull() && date(n, "date").isAfter(today); }
    private static boolean withinUntil(JsonNode deadline, JsonNode period) { return period.get("until").isNull() || (!deadline.isNull() && ordered(deadline, period.get("until"))); }
    private static boolean active(JsonNode p, LocalDate today) {
        return !value(p, "state").equals("unknown") && (p.get("from").isNull() || !date(p.get("from"), "from").isAfter(today)) && (p.get("until").isNull() || date(p.get("until"), "until").isAfter(today));
    }
}
