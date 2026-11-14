package com.task4.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.task4.config.AppConstants;
import com.task4.indexing.FileFingerprint;

public class ManifestStore {

    private static final String HEADER = "MANIFEST-V1";
    private static final String END = "END";
    private static final String SEPARATOR = "\t";

    private final Path file = Path.of(AppConstants.DATA_DIR, "manifest");

    public Map<String, FileFingerprint> load() throws IOException {
        Map<String, FileFingerprint> manifest = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            if (!HEADER.equals(reader.readLine())) {
                throw new CorruptDataException("Bad manifest header: " + file);
            }

            int count;
            try {
                count = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException e) {
                throw new CorruptDataException("Bad manifest count: " + file);
            }
            if (count < 0) {
                throw new CorruptDataException("Negative manifest count: " + file);
            }

            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new CorruptDataException("Manifest is truncated: " + file);
                }
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length != 4) {
                    throw new CorruptDataException("Bad manifest line: " + line);
                }
                try {
                    manifest.put(parts[0], new FileFingerprint(
                            Long.parseLong(parts[1]),
                            Long.parseLong(parts[2]),
                            parts[3]));
                } catch (NumberFormatException e) {
                    throw new CorruptDataException("Bad number in manifest line: " + line);
                }
            }

            if (!END.equals(reader.readLine())) {
                throw new CorruptDataException("Manifest end marker missing: " + file);
            }
        } catch (NoSuchFileException e) {
            return new LinkedHashMap<>(); // first run: no manifest yet
        }
        return manifest;
    }

    public void save(Map<String, FileFingerprint> manifest) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            writer.write(String.valueOf(manifest.size()));
            writer.newLine();

            for (Map.Entry<String, FileFingerprint> e : manifest.entrySet()) {
                String name = e.getKey();
                if (name.contains("\t") || name.contains("\n") || name.contains("\r")) {
                    throw new IOException("Unsupported character in file name: " + name);
                }
                FileFingerprint f = e.getValue();
                writer.write(name + SEPARATOR + f.size() + SEPARATOR
                        + f.lastModifiedMillis() + SEPARATOR + f.sha256());
                writer.newLine();
            }

            writer.write(END);
            writer.newLine();
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }

        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
    }
}