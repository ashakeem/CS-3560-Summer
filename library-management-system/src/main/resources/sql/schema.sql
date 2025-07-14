-- CS3560 Library Management System Database Schema

-- Drop tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS book_copies CASCADE;
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS students CASCADE;

-- Create Students table
CREATE TABLE students (
    bronco_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    degree VARCHAR(100) NOT NULL
);

-- Create Books table
CREATE TABLE books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    authors VARCHAR(500) NOT NULL,
    pages INTEGER NOT NULL,
    publisher VARCHAR(255) NOT NULL,
    pub_date DATE NOT NULL
);

-- Create Loans table
CREATE TABLE loans (
    loan_id SERIAL PRIMARY KEY,
    bronco_id VARCHAR(50) NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    FOREIGN KEY (bronco_id) REFERENCES students(bronco_id) ON DELETE CASCADE
);

-- Create Book Copies table
CREATE TABLE book_copies (
    barcode VARCHAR(50) PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'BORROWED')),
    loan_id INTEGER,
    FOREIGN KEY (isbn) REFERENCES books(isbn) ON DELETE CASCADE,
    FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_books_title ON books(LOWER(title));
CREATE INDEX idx_books_authors ON books(LOWER(authors));
CREATE INDEX idx_book_copies_isbn ON book_copies(isbn);
CREATE INDEX idx_book_copies_status ON book_copies(status);
CREATE INDEX idx_loans_bronco_id ON loans(bronco_id);
CREATE INDEX idx_loans_return_date ON loans(return_date);
CREATE INDEX idx_loans_due_date ON loans(due_date);

-- Sample data insertion (optional)
-- Students
INSERT INTO students (bronco_id, name, address, degree) VALUES
('B001', 'John Doe', '123 Main St, Pomona, CA', 'Computer Science'),
('B002', 'Jane Smith', '456 Oak Ave, Pomona, CA', 'Information Systems'),
('B003', 'Mike Johnson', '789 Pine Rd, Pomona, CA', 'Computer Engineering');

-- Books
INSERT INTO books (isbn, title, description, authors, pages, publisher, pub_date) VALUES
('978-0134685991', 'Effective Java', 'Best practices for Java programming', 'Joshua Bloch', 412, 'Addison-Wesley', '2018-01-06'),
('978-0135166307', 'Clean Code', 'A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 464, 'Prentice Hall', '2008-08-01'),
('978-0132350884', 'Design Patterns', 'Elements of Reusable Object-Oriented Software', 'Gamma, Helm, Johnson, Vlissides', 395, 'Addison-Wesley', '1994-10-31');

-- Book Copies
INSERT INTO book_copies (barcode, isbn, location, status) VALUES
('BC001', '978-0134685991', 'Shelf A1', 'AVAILABLE'),
('BC002', '978-0134685991', 'Shelf A1', 'AVAILABLE'),
('BC003', '978-0135166307', 'Shelf B2', 'AVAILABLE'),
('BC004', '978-0135166307', 'Shelf B2', 'AVAILABLE'),
('BC005', '978-0132350884', 'Shelf C3', 'AVAILABLE'); 