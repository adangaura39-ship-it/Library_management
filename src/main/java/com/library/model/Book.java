package com.library.model;

import java.util.Objects;

/**
 * Represents a book title in the catalog. Note: totalCopies/availableCopies
 * model *copies* of this title, not individual physical items.
 */
public class Book extends Entity {

    private String title;
    private String author;
    private String isbn;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(String title, String author, String isbn, String category, int totalCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public Book(int id, String title, String author, String isbn, String category,
                int totalCopies, int availableCopies) {
        super(id);
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public String getSummary() {
        return String.format("[#%d] \"%s\" by %s (ISBN %s) - %d/%d copies available",
                id, title, author, isbn, availableCopies, totalCopies);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
}
