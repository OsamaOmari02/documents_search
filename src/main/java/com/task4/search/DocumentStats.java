package com.task4.search;

import java.util.List;
import java.util.Map;

public record DocumentStats(
                int totalTokens,
                int uniqueTokens,
                List<Map.Entry<String, Integer>> topWords) {
}
