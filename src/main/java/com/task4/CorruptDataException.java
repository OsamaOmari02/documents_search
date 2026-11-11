package com.task4;

import java.io.IOException;

public class CorruptDataException extends IOException {

    public CorruptDataException(String message) {
        super(message);
    }
}
