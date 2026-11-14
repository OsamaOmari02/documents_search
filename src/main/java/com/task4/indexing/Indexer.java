package com.task4.indexing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.task4.config.AppConstants;
import com.task4.storage.CorruptDataException;
import com.task4.storage.ManifestStore;
import com.task4.storage.TokenStore;

public class Indexer {

    private final DocumentProcessor processor;
    private final TokenStore tokenStore;
    private final ManifestStore manifestStore;
    private final FileHasher hasher;

    public Indexer(DocumentProcessor processor, TokenStore tokenStore,
            ManifestStore manifestStore, FileHasher hasher) {
        this.processor = processor;
        this.tokenStore = tokenStore;
        this.manifestStore = manifestStore;
        this.hasher = hasher;
    }

    public IndexReport index() throws IOException {
        Map<String, FileFingerprint> manifest = loadManifest();
        Set<String> seen = new HashSet<>();
        int added = 0, updated = 0, touched = 0, unchanged = 0, deleted = 0, failed = 0;

        for (Path path : listTextFiles()) {
            String name = path.getFileName().toString();
            seen.add(name);

            if (name.contains("\t") || name.contains("\n") || name.contains("\r")) {
                System.err.println("Skipping " + name + ": unsupported character in file name");
                failed++;
                continue;
            }

            try {
                FileFingerprint old = manifest.get(name);
                long size = Files.size(path);
                long modified = Files.getLastModifiedTime(path).toMillis();
                boolean tokensExist = tokenStore.exists(name);

                // Fast path: same size and time, no file reading at all
                if (old != null && tokensExist
                        && old.size() == size && old.lastModifiedMillis() == modified) {
                    unchanged++;
                    continue;
                }

                // Size or time differs (or new): compare content
                FileFingerprint current = new FileFingerprint(size, modified, hasher.sha256(path));

                if (old != null && tokensExist && old.sha256().equals(current.sha256())) {
                    manifest.put(name, current); // touched only: no reprocessing
                    touched++;
                    continue;
                }

                tokenStore.save(name, processor.process(path));
                manifest.put(name, current); // manifest updated AFTER tokens are saved
                if (old == null) {
                    added++;
                } else {
                    updated++;
                }
            } catch (IOException e) {
                System.err.println("Could not index " + name + ": " + e.getMessage());
                failed++;
            }
        }

        // Deleted documents: in the manifest or in data/, but no longer in documents/
        Set<String> gone = new TreeSet<>(manifest.keySet());
        gone.addAll(tokenStore.listDocuments());
        gone.removeAll(seen);
        for (String name : gone) {
            try {
                tokenStore.delete(name);
                manifest.remove(name);
                deleted++;
            } catch (IOException e) {
                System.err.println("Could not remove " + name + ": " + e.getMessage());
                failed++;
            }
        }

        if (added + updated + touched + deleted > 0) {
            manifestStore.save(manifest);
        }
        return new IndexReport(added, updated, touched, unchanged, deleted, failed);
    }

    private Map<String, FileFingerprint> loadManifest() throws IOException {
        try {
            return manifestStore.load();
        } catch (CorruptDataException e) {
            System.err.println("Manifest unreadable (" + e.getMessage()
                    + "); reprocessing all documents.");
            return new LinkedHashMap<>();
        }
    }

    private List<Path> listTextFiles() throws IOException {
        try (var files = Files.list(Path.of(AppConstants.DOCUMENTS_DIR))) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .sorted()
                    .toList();
        }
    }
}