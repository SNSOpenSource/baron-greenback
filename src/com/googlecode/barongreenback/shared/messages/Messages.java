package com.googlecode.barongreenback.shared.messages;

import com.googlecode.funclate.Model;

import static com.googlecode.funclate.Model.model;

public class Messages {
    public static Model messageModel(String message, Category category) {
        return model().add("message", model().add("text", message).add("category", category));
    }

    public static Model success(String message) {
        return messageModel(message, Category.SUCCESS);
    }

    public static Model error(String message) {
        return messageModel(message, Category.ERROR);
    }
}
