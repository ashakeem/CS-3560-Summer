package com.cs3560.library.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {
    @Id
    private String barcode;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan currentLoan;

    // Enum for book status
    public enum BookStatus {
        AVAILABLE, BORROWED
    }

    // Default constructor
    public BookCopy() {}

    // Constructor
    public BookCopy(String barcode, String location, Book book) {
        this.barcode = barcode;
        this.location = location;
        this.book = book;
        this.status = BookStatus.AVAILABLE;
    }

    // Getters and Setters
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Loan getCurrentLoan() {
        return currentLoan;
    }

    public void setCurrentLoan(Loan currentLoan) {
        this.currentLoan = currentLoan;
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "barcode='" + barcode + '\'' +
                ", location='" + location + '\'' +
                ", status=" + status +
                ", book=" + (book != null ? book.getTitle() : "null") +
                '}';
    }
} 