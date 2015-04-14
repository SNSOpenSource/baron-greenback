package com.googlecode.barongreenback.shared.messages;

public enum Category {
    SUCCESS("success"), ERROR("error");
    private final String category;

    private Category(String category) {
        this.category = category;
    }


    @Override
    public String toString() {
        return category;
    }

    public static Category fromString(String category) {
        return Category.valueOf(category.toUpperCase());
    }
}