package com.task4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class TokenStore {

    private static final String HEADER = "TOKENS-V1";
    private static final String END = "END";

    public void save(String documentName, List<String> tokens) throws IOException {
        Files.createDirectories(Path.of(AppConstants.DATA_DIR));
        Path target = pathFor(documentName);
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            writer.write(String.valueOf(tokens.size()));
            writer.newLine();
            for (String token : tokens) {
                writer.write(token);
                writer.newLine();
            }
            writer.write(END);
            writer.newLine();
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }

        Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
    }

    public List<String> load(String documentName) throws IOException {
        Path file = pathFor(documentName);

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            if (!HEADER.equals(reader.readLine())) {
                throw new CorruptDataException("Bad header: " + file);
            }

            int count;
            try {
                count = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException e) {
                throw new CorruptDataException("Bad token count: " + file);
            }
            if (count < 0) {
                throw new CorruptDataException("Negative token count: " + file);
            }

            List<String> tokens = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new CorruptDataException("File is truncated: " + file);
                }
                tokens.add(line);
            }

            if (!END.equals(reader.readLine())) {
                throw new CorruptDataException("Missing end marker: " + file);
            }
            return tokens;
        }
    }

    private Path pathFor(String documentName) {
        return Path.of(AppConstants.DATA_DIR, documentName + ".tokens");
    }
}
