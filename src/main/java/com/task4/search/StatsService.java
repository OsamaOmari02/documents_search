package com.task4.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.task4.storage.TokenStore;

public class StatsService {

    private static final int TOP_N = 10;

    private final TokenStore tokenStore;

    public StatsService(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public DocumentStats stats(String documentName) throws IOException {
        List<String> tokens = tokenStore.load(documentName);

        Map<String, Integer> counts = new HashMap<>();
        for (String token : tokens) {
            counts.merge(token, 1, Integer::sum);
        }

        List<Map.Entry<String, Integer>> top = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_N)
                .toList();

        return new DocumentStats(tokens.size(), counts.size(), top);
    }
}
