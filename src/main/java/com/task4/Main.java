package com.task4;

import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Tokenizer tokenizer = new Tokenizer(Set.of("the", "a", "is", "and", "of"));
        System.out.println(tokenizer.tokenize("Java is a powerful programming language."));
        System.out.println(tokenizer.tokenize("JAVA, java! Java?"));
        System.out.println(tokenizer.tokenize("well-known don't café 2024 مرحبا بالعالم"));
    }
}
