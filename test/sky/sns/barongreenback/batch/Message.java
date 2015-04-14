package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.messages.Category;

import static java.lang.String.format;

public class Message {
    private final Category category;
    private final String message;

    public Message(String message, Category category) {
        this.message = message;
        this.category = category;
    }

    public Category category() {
        return category;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return format("%s: '%s'", category, message);
    }
}
