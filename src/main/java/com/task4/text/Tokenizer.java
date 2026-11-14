package com.task4.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.task4.config.AppConstants;

public class Tokenizer {

    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");

    public List<String> tokenize(String text) {
        Objects.requireNonNull(text);
        List<String> tokens = new ArrayList<>();
        String cleaned = text.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace("\u2019", "");
        Matcher matcher = WORD.matcher(cleaned);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() < 2 || AppConstants.STOP_WORDS.contains(word)) {
                continue;
            }
            tokens.add(word);
        }
        return tokens;
    }

}
