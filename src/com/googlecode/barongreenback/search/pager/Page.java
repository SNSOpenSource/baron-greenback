package com.googlecode.barongreenback.search.pager;

public class Page {
    private String text;
    private String cssClass;
    private String link;

    public Page(String text, String cssClass) {
        this(text, cssClass, null);
    }

    public Page(String text, String cssClass, String link) {
        this.text = text;
        this.cssClass = cssClass;
        this.link = link;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Page{" +
                "text='" + text + '\'' +
                ", cssClass='" + cssClass + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (!cssClass.equals(page.cssClass)) return false;
        if (link != null ? !link.equals(page.link) : page.link != null) return false;
        if (!text.equals(page.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + cssClass.hashCode();
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }
}
