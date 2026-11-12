package com.task4;

public record SearchResult(String document, int matchedWords, int occurrences, double score) {}
