package org.miniboot.app.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExtractHelper {
    public static Optional<Integer> extractInt(Map<String, List<String>> q, String key) {
        if (q == null)
            return Optional.empty();
        List<String> values = q.get(key);
        if (values == null || values.isEmpty())
            return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(values.get(0)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> extractString(Map<String, List<String>> query, String key) {
        if (query.containsKey(key) && !query.get(key).isEmpty()) {
            return Optional.of(query.get(key).get(0));
        }
        return Optional.empty();
    }

    public static Optional<String> extractFirst(Map<String, List<String>> q, String key) {
        if (q == null)
            return Optional.empty();
        List<String> values = q.get(key);
        if (values == null || values.isEmpty())
            return Optional.empty();
        return Optional.of(values.get(0));
    }

    public static Optional<Integer> extractId(Map<String, List<String>> queries) {
        if (queries == null)
            return Optional.empty();
        List<String> ids = queries.get("id");
        if (ids == null || ids.isEmpty())
            return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(ids.get(0)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
