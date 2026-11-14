package com.task4.config;

import java.util.Set;

public class AppConstants {

    public static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "but", "if", "of", "to", "in", "on", "at",
            "by", "for", "with", "from", "as", "is", "are", "was", "were", "be", "been",
            "it", "its", "this", "that", "these", "those", "i", "you", "he", "she",
            "we", "they", "me", "him", "her", "us", "them", "my", "your", "our", "their",
            "not", "no", "so", "than", "very", "can", "will", "just",
            "have", "has", "had", "do", "does", "did", "over", "up", "out", "into",
            "about", "because", "every", "all", "any", "some", "such", "also", "dont");
    public static final String DOCUMENTS_DIR = "documents";
    public static final String DATA_DIR = "data";

}
