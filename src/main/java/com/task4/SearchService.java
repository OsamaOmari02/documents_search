package com.task4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchService {

    private static final String SUFFIX = ".tokens";

    private final Tokenizer tokenizer;
    private final TokenStore tokenStore;

    public SearchService(Tokenizer tokenizer, TokenStore tokenStore) {
        this.tokenizer = tokenizer;
        this.tokenStore = tokenStore;
    }

    public List<SearchResult> search(String query) throws IOException {
        Set<String> queryWords = new LinkedHashSet<>(tokenizer.tokenize(query));
        if (queryWords.isEmpty()) {
            return List.of();
        }
        List<SearchResult> results = new ArrayList<>();

        for (String document : listDocuments()) {
            List<String> tokens;
            try {
                tokens = tokenStore.load(document);
            } catch (IOException e) {
                System.err.println("Skipping " + document + ": " + e.getMessage());
                continue;
            }
            Map<String, Integer> counts = new HashMap<>();
            for (String token : tokens) {
                counts.merge(token, 1, Integer::sum);
            }
            int matched = 0;
            int occurrences = 0;
            for (String word : queryWords) {
                int count = counts.getOrDefault(word, 0);
                if (count > 0) {
                    matched++;
                    occurrences += count;
                }
            }
            if (matched > 0) {
                results.add(new SearchResult(document, matched, occurrences));
            }

        }
        return results;

    }

    private List<String> listDocuments() {
        try (var files = Files.list(Path.of(AppConstants.DATA_DIR))) {
            return files
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(SUFFIX))
                    .map(n -> n.substring(0, n.length() - SUFFIX.length()))
                    .sorted()
                    .toList();

        } catch (IOException e) {
            return List.of();
        }
    }
}
