package com.task4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        Tokenizer tokenizer = new Tokenizer();
        DocumentProcessor processor = new DocumentProcessor(tokenizer);
        try (var files = Files.list(Path.of(AppConstants.DOCUMENTS_DIR))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".txt")).sorted().toList()) {
                if (p.getFileName().toString().equals("document8.txt")) { // file is too long to process, so we skip it for now
                    continue;
                }
                System.out.println(p.getFileName() + " -> " + processor.process(p));
            }
        } catch (IOException ex) {
            System.getLogger(Main.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
