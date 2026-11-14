package com.task4.indexing;

public record IndexReport(int added, int updated, int touched,
        int unchanged, int deleted, int failed) {
}