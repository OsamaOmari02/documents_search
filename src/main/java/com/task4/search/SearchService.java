package com.task4.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.task4.storage.TokenStore;
import com.task4.text.Tokenizer;

public class SearchService {

    private final Tokenizer tokenizer;
    private final TokenStore tokenStore;

    public SearchService(Tokenizer tokenizer, TokenStore tokenStore) {
        this.tokenizer = tokenizer;
        this.tokenStore = tokenStore;
    }

    private static final Comparator<SearchResult> BY_RELEVANCE = Comparator.comparingDouble(SearchResult::score)
            .reversed()
            .thenComparing(Comparator.comparingInt(SearchResult::matchedWords).reversed())
            .thenComparing(SearchResult::document);

    public List<SearchResult> search(String query) throws IOException {
        Set<String> queryWords = new LinkedHashSet<>(tokenizer.tokenize(query));
        if (queryWords.isEmpty()) {
            return List.of();
        }
        List<SearchResult> results = new ArrayList<>();

        for (String document : tokenStore.listDocuments()) {
            try {
                evaluate(document, queryWords).ifPresent(results::add);
            } catch (IOException e) {
                System.err.println("Skipping " + document + ": " + e.getMessage());
                continue;
            }
        }
        results.sort(BY_RELEVANCE);
        return results;

    }

    public Optional<SearchResult> searchInDocument(String document, String query) throws IOException {
        Set<String> queryWords = new LinkedHashSet<>(tokenizer.tokenize(query));
        if (queryWords.isEmpty()) {
            return Optional.empty();
        }
        return evaluate(document, queryWords);
    }

    public List<SearchResult> searchPhrase(String phrase) throws IOException {
        List<String> words = tokenizer.tokenize(phrase);
        if (words.isEmpty()) {
            return List.of();
        }

        List<SearchResult> results = new ArrayList<>();
        for (String document : tokenStore.listDocuments()) {
            try {
                evaluatePhrase(document, words).ifPresent(results::add);
            } catch (IOException e) {
                System.err.println("Skipping " + document + ": " + e.getMessage());
            }
        }
        results.sort(BY_RELEVANCE);
        return results;
    }

    public Optional<SearchResult> searchPhraseInDocument(String document, String phrase) throws IOException {
        List<String> words = tokenizer.tokenize(phrase);
        if (words.isEmpty()) {
            return Optional.empty();
        }
        return evaluatePhrase(document, words);
    }

    private Optional<SearchResult> evaluate(String document, Set<String> queryWords) throws IOException {
        List<String> tokens = tokenStore.load(document);

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

        if (matched == 0) {
            return Optional.empty();
        }
        return Optional.of(new SearchResult(document, matched, occurrences, score(matched, occurrences)));
    }

    private Optional<SearchResult> evaluatePhrase(String document, List<String> phrase) throws IOException {
        List<String> tokens = tokenStore.load(document);
        int n = phrase.size();

        int occurrences = 0;
        for (int i = 0; i + n <= tokens.size(); i++) {
            if (tokens.subList(i, i + n).equals(phrase)) {
                occurrences++;
            }
        }

        if (occurrences == 0) {
            return Optional.empty();
        }
        return Optional.of(new SearchResult(document, n, occurrences, score(n, occurrences)));
    }

    private double score(int matchedWords, int occurrences) {
        return matchedWords * 100.0 + 50.0 * occurrences / (occurrences + 5.0);
    }

}
