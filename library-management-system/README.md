# CS3560 Library Management System

A comprehensive JavaFX-based library management system for CS3560, featuring student management, book cataloging, loan processing, and reporting capabilities.

## Features

- **Student Management**: CRUD operations for student records
- **Book Catalog**: Manage books and book copies with ISBN tracking
- **Loan Management**: 
  - Multi-step loan creation wizard
  - Enforce business rules (max 5 loans, no overdue)
  - Atomic loan returns
  - On-screen loan receipts
- **Reporting**: Generate and export loan reports with filtering options
- **Search**: Fast book search by title, author, or ISBN
- **Performance**: Optimized for up to 10,000 books with < 2 second response time

## Technology Stack

- **Java 17+**
- **JavaFX 21** - GUI Framework
- **Hibernate 6.4** - ORM
- **PostgreSQL** - Database
- **Maven** - Build tool

## Prerequisites

1. **Java 17 or higher**
2. **PostgreSQL** installed and running
3. **Maven** installed

## Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE library_db;
```

2. Update database credentials in `src/main/resources/hibernate.cfg.xml`:
```xml
<property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/library_db</property>
<property name="hibernate.connection.username">postgres</property>
<property name="hibernate.connection.password">your_password</property>
```

3. The application will automatically create tables on first run. Alternatively, use the SQL script at `src/main/resources/sql/schema.sql`

## Building and Running

1. Clone the repository:
```bash
git clone <repository-url>
cd library-management-system
```

2. Build the project:
```bash
mvn clean compile
```

3. Run the application:
```bash
mvn javafx:run
```

## Project Structure

```
library-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cs3560/library/
│   │   │       ├── model/          # JPA Entities
│   │   │       ├── dao/            # Data Access Objects
│   │   │       ├── service/        # Business Logic
│   │   │       ├── ui/             # JavaFX Controllers
│   │   │       └── util/           # Utilities
│   │   └── resources/
│   │       ├── fxml/               # JavaFX FXML Views
│   │       ├── sql/                # Database Scripts
│   │       └── hibernate.cfg.xml   # Hibernate Configuration
│   └── test/
└── pom.xml
```

## Usage Guide

### Student Management
1. Navigate to the "Students" tab
2. Use Add/Edit/Delete buttons for CRUD operations
3. Search students by name

### Book Management
1. Navigate to the "Books" tab
2. Add books with ISBN, title, authors, etc.
3. Search books using the search field

### Book Copy Management
1. Navigate to the "Book Copies" tab
2. Add physical copies with unique barcodes
3. Filter by availability status

### Creating Loans
1. Navigate to "Loans" → "Create New Loan"
2. Follow the wizard:
   - Step 1: Select student
   - Step 2: Search and select book copies
   - Step 3: Set due date (max 180 days)
   - Step 4: Review and confirm
3. Print or save the receipt

### Returning Loans
1. Navigate to "Loans" → "Return Loans"
2. Select the loan from dropdown
3. Click "Return Loan" to return all items

### Generating Reports
1. Navigate to "Reports" tab
2. Select filters:
   - Student (optional)
   - Date range
   - Status filter
3. Click "Generate Report"
4. Export to CSV if needed

## Business Rules

- Students cannot have more than 5 active loans
- Students with overdue loans cannot borrow new items
- Due dates must be within 180 days from borrow date
- All items in a loan must be returned together
- Book copies can only be borrowed if available

## Performance Considerations

- Indexed database columns for fast searching
- Lazy loading for related entities
- Connection pooling configured
- Optimized queries for report generation

## Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials in hibernate.cfg.xml
- Ensure database 'library_db' exists

### JavaFX Module Issues
- Ensure Java 17+ is being used
- Check that JavaFX modules are properly included in pom.xml

### Build Issues
- Run `mvn clean` before building
- Ensure all Maven dependencies are downloaded

## License

This project is for educational purposes as part of CS3560 coursework. 