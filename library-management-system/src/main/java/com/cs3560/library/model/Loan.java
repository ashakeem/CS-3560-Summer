package com.cs3560.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bronco_id", nullable = false)
    private Student student;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @OneToMany(mappedBy = "currentLoan", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BookCopy> bookCopies = new ArrayList<>();

    // Default constructor
    public Loan() {}

    // Constructor
    public Loan(Student student, LocalDate borrowDate, LocalDate dueDate) {
        this.student = student;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    // Helper methods
    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }

    // Getters and Setters
    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public List<BookCopy> getBookCopies() {
        return bookCopies;
    }

    public void setBookCopies(List<BookCopy> bookCopies) {
        this.bookCopies = bookCopies;
    }

    public void addBookCopy(BookCopy bookCopy) {
        bookCopies.add(bookCopy);
        bookCopy.setCurrentLoan(this);
    }

    public void removeBookCopy(BookCopy bookCopy) {
        bookCopies.remove(bookCopy);
        bookCopy.setCurrentLoan(null);
    }

    @Override
    public String toString() {
        return "Loan #" + loanId + " - " + student.getName() + 
               " (Due: " + dueDate + ", Returned: " + (isReturned() ? returnDate : "Not returned") + ")";
    }
} 