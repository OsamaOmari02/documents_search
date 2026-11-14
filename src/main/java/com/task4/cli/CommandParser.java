package com.task4.cli;

import java.util.Locale;

public class CommandParser {

    private static final String SEARCH_USAGE = "Usage: search <words> | search \"<phrase>\" | search <file.txt> <words>";

    public Command parse(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        String name = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : "";

        return switch (name) {
            case "index" -> new Command.Index();
            case "search" -> parseSearch(arg);
            case "stats" -> arg.isEmpty()
                    ? new Command.Invalid("Usage: stats <file>")
                    : new Command.Stats(withExtension(arg));
            case "list" -> new Command.ListDocuments();
            case "help" -> new Command.Help();
            case "exit", "quit" -> new Command.Exit();
            default -> new Command.Invalid("Unknown command: " + parts[0] + ". Type 'help'.");
        };
    }

    private Command parseSearch(String arg) {
        if (arg.isEmpty()) {
            return new Command.Invalid(SEARCH_USAGE);
        }

        String first = arg.split("\\s+", 2)[0];
        if (first.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            String rest = arg.substring(first.length()).trim();
            if (rest.isEmpty()) {
                return new Command.Invalid(SEARCH_USAGE);
            }
            return buildSearch(first, rest);
        }
        return buildSearch(null, arg);
    }

    private Command buildSearch(String document, String text) {
        if (isQuoted(text)) {
            String phrase = text.substring(1, text.length() - 1).trim();
            if (phrase.isEmpty()) {
                return new Command.Invalid("The phrase is empty.");
            }
            return new Command.Search(document, phrase, true);
        }
        return new Command.Search(document, text, false);
    }

    private boolean isQuoted(String s) {
        return s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"");
    }

    private String withExtension(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".txt") ? name : name + ".txt";
    }
}