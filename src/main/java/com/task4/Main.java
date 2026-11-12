package com.task4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Tokenizer tokenizer = new Tokenizer();
        DocumentProcessor processor = new DocumentProcessor(tokenizer);
        TokenStore tokenStore = new TokenStore();

        // List<Path> txtFiles;
        // try (var files = Files.list(Path.of(AppConstants.DOCUMENTS_DIR))) {
        //     txtFiles = files
        //             .filter(f -> f.toString().endsWith(".txt"))
        //             .sorted()
        //             .toList();
        // } catch (IOException e) {
        //     System.err.println("Could not read documents folder: " + e.getMessage());
        //     return;
        // }
        // if (txtFiles.isEmpty()) {
        //     System.out.println("No .txt files found in '" + AppConstants.DOCUMENTS_DIR + "'.");
        //     return;
        // }
        // for (Path path : txtFiles) {
        //     String name = path.getFileName().toString();
        //     try {
        //         List<String> tokens = processor.process(path);
        //         tokenStore.save(name, tokens);
        //         List<String> loaded = tokenStore.load(name);
        //         boolean IsSame = loaded.equals(tokens);
        //         System.out.println(name + " -> " + tokens.size() + " tokens, saved and reloaded, equal: " + IsSame);
        //     } catch (CorruptDataException e) {
        //         System.err.println(name + " -> corrupt saved data: " + e.getMessage());
        //     } catch (IOException e) {
        //         System.err.println(name + " -> failed: " + e.getMessage());
        //     }
        // }
        SearchService searchService = new SearchService(tokenizer, tokenStore);
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) {
                break;
            }

            if (line.startsWith("search ")) {
                String query = line.substring("search ".length());
                try {
                    List<SearchResult> results = searchService.search(query);
                    if (results.isEmpty()) {
                        System.out.println("No documents found.");
                    }
                    for (SearchResult r : results) {
                        System.out.println(r.document());
                        System.out.println("  Matched words: " + r.matchedWords());
                        System.out.println("  Occurrences: " + r.occurrences());
                    }
                } catch (IOException e) {
                    System.err.println("Search failed: " + e.getMessage());
                }
            } else {
                System.out.println("Unknown command. Try: search <words>, exit");
            }
        }

    }
}
