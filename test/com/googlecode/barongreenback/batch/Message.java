package com.googlecode.barongreenback.batch;

import com.googlecode.barongreenback.shared.messages.Category;

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
}
