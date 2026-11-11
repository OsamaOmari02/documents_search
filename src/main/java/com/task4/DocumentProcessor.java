package com.task4;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DocumentProcessor {

    private final Tokenizer tokenizer;

    public DocumentProcessor(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public List<String> process(Path file) throws IOException {
        List<String> tokens = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                tokens.addAll(tokenizer.tokenize(line));
            }
        }
        return tokens;
    }
}
