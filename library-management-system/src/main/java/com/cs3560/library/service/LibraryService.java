package com.cs3560.library.service;

import com.cs3560.library.dao.*;
import com.cs3560.library.model.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LibraryService {
    private final StudentDao studentDao;
    private final BookDao bookDao;
    private final BookCopyDao bookCopyDao;
    private final LoanDao loanDao;

    public LibraryService() {
        this.studentDao = new StudentDaoImpl();
        this.bookDao = new BookDaoImpl();
        this.bookCopyDao = new BookCopyDaoImpl();
        this.loanDao = new LoanDaoImpl();
    }

    // Create a new loan
    public Loan createLoan(String broncoId, List<String> barcodes, LocalDate dueDate) {
        // Validate student
        Optional<Student> studentOpt = studentDao.findByBroncoId(broncoId);
        if (!studentOpt.isPresent()) {
            throw new IllegalArgumentException("Student not found with ID: " + broncoId);
        }
        Student student = studentOpt.get();

        // Check for overdue loans
        if (studentDao.hasOverdueLoans(broncoId)) {
            throw new IllegalStateException("Student has overdue loans and cannot borrow more books");
        }

        // Check active loan count
        int activeLoanCount = studentDao.countActiveLoans(broncoId);
        if (activeLoanCount >= 5) {
            throw new IllegalStateException("Student already has 5 active loans");
        }

        // Validate due date (must be within 180 days)
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today) || dueDate.isAfter(today.plusDays(180))) {
            throw new IllegalArgumentException("Due date must be between today and 180 days from today");
        }

        // Validate and collect book copies
        List<BookCopy> bookCopies = new ArrayList<>();
        for (String barcode : barcodes) {
            Optional<BookCopy> copyOpt = bookCopyDao.findByBarcode(barcode);
            if (!copyOpt.isPresent()) {
                throw new IllegalArgumentException("Book copy not found with barcode: " + barcode);
            }
            BookCopy copy = copyOpt.get();
            if (copy.getStatus() != BookCopy.BookStatus.AVAILABLE) {
                throw new IllegalStateException("Book copy is not available: " + barcode);
            }
            bookCopies.add(copy);
        }

        // Create loan
        Loan loan = new Loan(student, today, dueDate);
        
        // Add book copies and update their status
        for (BookCopy copy : bookCopies) {
            loan.addBookCopy(copy);
            copy.setStatus(BookCopy.BookStatus.BORROWED);
        }

        // Save loan (this will cascade to update book copies)
        return loanDao.save(loan);
    }

    // Return a loan
    public Loan returnLoan(Long loanId) {
        return loanDao.returnLoan(loanId);
    }

    // Search books by title, author, or ISBN
    public List<BookSearchResult> searchBooks(String searchTerm) {
        List<Book> books = bookDao.searchByTitleOrAuthorOrIsbn(searchTerm);
        
        return books.stream().map(book -> {
            List<BookCopy> allCopies = bookCopyDao.findByIsbn(book.getIsbn());
            List<BookCopy> availableCopies = allCopies.stream()
                .filter(copy -> copy.getStatus() == BookCopy.BookStatus.AVAILABLE)
                .collect(Collectors.toList());
            
            List<BookCopy> borrowedCopies = allCopies.stream()
                .filter(copy -> copy.getStatus() == BookCopy.BookStatus.BORROWED)
                .collect(Collectors.toList());
            
            return new BookSearchResult(book, allCopies.size(), availableCopies.size(), 
                                       borrowedCopies, availableCopies);
        }).collect(Collectors.toList());
    }

    // Generate loan report
    public List<Loan> generateReport(String broncoId, LocalDate startDate, LocalDate endDate) {
        if (broncoId != null && !broncoId.isEmpty()) {
            return loanDao.findLoansByStudentAndDateRange(broncoId, startDate, endDate);
        } else {
            return loanDao.findLoansByDateRange(startDate, endDate);
        }
    }

    // Additional helper methods
    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public List<Book> getAllBooks() {
        return bookDao.findAll();
    }

    public List<BookCopy> getAllBookCopies() {
        return bookCopyDao.findAll();
    }

    public List<Loan> getActiveLoans() {
        return loanDao.findActiveLoans();
    }

    public List<Loan> getOverdueLoans() {
        return loanDao.findOverdueLoans();
    }

    // Inner class for book search results
    public static class BookSearchResult {
        private final Book book;
        private final int totalCopies;
        private final int availableCopies;
        private final List<BookCopy> borrowedCopies;
        private final List<BookCopy> availableCopiesList;

        public BookSearchResult(Book book, int totalCopies, int availableCopies, 
                               List<BookCopy> borrowedCopies, List<BookCopy> availableCopiesList) {
            this.book = book;
            this.totalCopies = totalCopies;
            this.availableCopies = availableCopies;
            this.borrowedCopies = borrowedCopies;
            this.availableCopiesList = availableCopiesList;
        }

        // Getters
        public Book getBook() {
            return book;
        }

        public int getTotalCopies() {
            return totalCopies;
        }

        public int getAvailableCopies() {
            return availableCopies;
        }

        public List<BookCopy> getBorrowedCopies() {
            return borrowedCopies;
        }

        public List<BookCopy> getAvailableCopiesList() {
            return availableCopiesList;
        }

        public List<LocalDate> getDueDates() {
            return borrowedCopies.stream()
                .map(copy -> copy.getCurrentLoan())
                .filter(loan -> loan != null)
                .map(Loan::getDueDate)
                .collect(Collectors.toList());
        }
    }
} 