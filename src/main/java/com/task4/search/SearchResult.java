package com.task4.search;

public record SearchResult(String document, int matchedWords, int occurrences, double score) {
}
