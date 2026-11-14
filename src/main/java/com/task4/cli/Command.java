package com.task4.cli;

public sealed interface Command {

    record Index() implements Command {
    }

    record Search(String document, String text, boolean phrase) implements Command {
    }

    record Stats(String document) implements Command {
    }

    record ListDocuments() implements Command {
    }

    record Help() implements Command {
    }

    record Exit() implements Command {
    }

    record Invalid(String message) implements Command {
    }
}