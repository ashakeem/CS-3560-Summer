package com.cs3560.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {
    @Id
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String authors;

    @Column(nullable = false)
    private int pages;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "pub_date", nullable = false)
    private LocalDate pubDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookCopy> copies = new ArrayList<>();

    // Default constructor
    public Book() {}

    // Constructor
    public Book(String isbn, String title, String description, String authors, 
                int pages, String publisher, LocalDate pubDate) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.pages = pages;
        this.publisher = publisher;
        this.pubDate = pubDate;
    }

    // Getters and Setters
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDate pubDate) {
        this.pubDate = pubDate;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }

    @Override
    public String toString() {
        return title + " (ISBN: " + isbn + ")";
    }
} 