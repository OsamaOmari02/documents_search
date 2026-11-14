package com.task4.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.task4.cli.Command.Exit;
import com.task4.cli.Command.Help;
import com.task4.cli.Command.Invalid;
import com.task4.cli.Command.ListDocuments;
import com.task4.cli.Command.Search;
import com.task4.cli.Command.Stats;
import com.task4.indexing.IndexReport;
import com.task4.indexing.Indexer;
import com.task4.search.DocumentStats;
import com.task4.search.SearchResult;
import com.task4.search.SearchService;
import com.task4.search.StatsService;
import com.task4.storage.TokenStore;

public class Cli {

    private final Indexer indexer;
    private final CommandParser commandParser;
    private final SearchService searchService;
    private final StatsService statsService;
    private final TokenStore tokenStore;

    public Cli(Indexer indexer, CommandParser parser, SearchService searchService,
            StatsService statsService, TokenStore tokenStore) {
        this.indexer = indexer;
        this.commandParser = parser;
        this.searchService = searchService;
        this.statsService = statsService;
        this.tokenStore = tokenStore;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        printHelp();

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            Command command = commandParser.parse(line);
            if (command instanceof Command.Exit) {
                break;
            }
            execute(command);
        }
    }

    private void execute(Command command) {
        try {
            switch (command) {
                case Command.Index _ -> handleIndex();
                case Command.Search s -> handleSearch(s);
                case Command.Stats s -> handleStats(s);
                case Command.ListDocuments _ -> handleList();
                case Command.Help _ -> printHelp();
                case Command.Invalid i -> System.out.println(i.message());
                case Command.Exit _ -> {
                }
            }
        } catch (IOException e) {
            System.err.println("Operation failed: " + e.getMessage());
        }
    }

    private void handleIndex() throws IOException {
        long start = System.nanoTime();
        try {
            IndexReport r = indexer.index();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("Indexed in " + ms + " ms: "
                    + r.added() + " new, " + r.updated() + " updated, "
                    + r.touched() + " touched (same content), "
                    + r.unchanged() + " unchanged, "
                    + r.deleted() + " deleted, " + r.failed() + " failed");
        } catch (NoSuchFileException e) {
            System.out.println("Documents folder not found: " + e.getMessage());
        }
    }

    private void handleSearch(Command.Search s) throws IOException {
        if (s.document() == null) {
            List<SearchResult> results = s.phrase()
                    ? searchService.searchPhrase(s.text())
                    : searchService.search(s.text());
            if (results.isEmpty()) {
                System.out.println("No documents found.");
                return;
            }
            results.forEach(this::printResult);
            return;
        }

        try {
            Optional<SearchResult> result = s.phrase()
                    ? searchService.searchPhraseInDocument(s.document(), s.text())
                    : searchService.searchInDocument(s.document(), s.text());
            result.ifPresentOrElse(
                    this::printResult,
                    () -> System.out.println("No match in " + s.document()));
        } catch (NoSuchFileException e) {
            System.out.println("Document not found: " + s.document());
        }
    }

    private void handleStats(Command.Stats s) throws IOException {
        try {
            DocumentStats stats = statsService.stats(s.document());
            System.out.println(s.document());
            System.out.println("  Total tokens: " + stats.totalTokens());
            System.out.println("  Unique tokens: " + stats.uniqueTokens());
            if (stats.topWords().isEmpty()) {
                System.out.println("  No words found.");
                return;
            }
            System.out.println("  Most frequent words:");
            for (Map.Entry<String, Integer> e : stats.topWords()) {
                System.out.println("    " + e.getKey() + " - " + e.getValue());
            }
        } catch (NoSuchFileException e) {
            System.out.println("Document not found: " + s.document());
        }
    }

    private void handleList() throws IOException {
        List<String> documents = tokenStore.listDocuments();
        if (documents.isEmpty()) {
            System.out.println("No processed documents.");
            return;
        }
        documents.forEach(System.out::println);
    }

    private void printResult(SearchResult r) {
        System.out.println(r.document());
        System.out.println("  Matched words: " + r.matchedWords());
        System.out.println("  Occurrences: " + r.occurrences());
        System.out.println("  Score: " + String.format(Locale.ROOT, "%.1f", r.score()));
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  index    process new and changed documents");
        System.out.println("  search <words>             search all documents");
        System.out.println("  search \"<phrase>\"          exact phrase search");
        System.out.println("  search <file.txt> <words>  search one document (phrase works too)");
        System.out.println("  stats <file>               document statistics");
        System.out.println("  list                       list processed documents");
        System.out.println("  help                       show this help");
        System.out.println("  exit                       quit");
    }
}