package com.task4;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");

    private final Set<String> stopWords;

    public Tokenizer(Set<String> stopWords) {
        this.stopWords = Set.copyOf(stopWords);
    }

    public List<String> tokenize(String text) {
        Objects.requireNonNull(text);
        List<String> tokens = new ArrayList<>();
        String cleaned = text.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace("\u2019", "");
        Matcher matcher = WORD.matcher(cleaned);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() < 2 || stopWords.contains(word)) {
                continue;
            }
            tokens.add(word);
        }
        return tokens;
    }

}
