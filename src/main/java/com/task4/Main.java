package com.task4;

import java.io.IOException;

import com.task4.cli.Cli;
import com.task4.cli.CommandParser;
import com.task4.indexing.DocumentProcessor;
import com.task4.indexing.FileHasher;
import com.task4.indexing.Indexer;
import com.task4.search.SearchService;
import com.task4.search.StatsService;
import com.task4.storage.ManifestStore;
import com.task4.storage.TokenStore;
import com.task4.text.Tokenizer;

public class Main {

    public static void main(String[] args) throws IOException {
        Tokenizer tokenizer = new Tokenizer();
        TokenStore tokenStore = new TokenStore();
        DocumentProcessor processor = new DocumentProcessor(tokenizer);
        Indexer indexer = new Indexer(processor, tokenStore, new ManifestStore(), new FileHasher());
        SearchService searchService = new SearchService(tokenizer, tokenStore);
        StatsService statsService = new StatsService(tokenStore);

        new Cli(indexer, new CommandParser(), searchService, statsService, tokenStore).run();

    }
}
