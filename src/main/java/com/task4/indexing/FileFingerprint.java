package com.task4.indexing;

public record FileFingerprint(long size, long lastModifiedMillis, String sha256) {
}