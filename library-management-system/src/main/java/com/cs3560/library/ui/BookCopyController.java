package com.cs3560.library.ui;

import com.cs3560.library.dao.BookCopyDao;
import com.cs3560.library.dao.BookCopyDaoImpl;
import com.cs3560.library.dao.BookDao;
import com.cs3560.library.dao.BookDaoImpl;
import com.cs3560.library.model.Book;
import com.cs3560.library.model.BookCopy;
import com.cs3560.library.model.Loan;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookCopyController {
    @FXML private TableView<BookCopy> bookCopyTable;
    @FXML private TableColumn<BookCopy, String> barcodeColumn;
    @FXML private TableColumn<BookCopy, String> isbnColumn;
    @FXML private TableColumn<BookCopy, String> bookTitleColumn;
    @FXML private TableColumn<BookCopy, String> locationColumn;
    @FXML private TableColumn<BookCopy, BookCopy.BookStatus> statusColumn;
    @FXML private TableColumn<BookCopy, String> dueDateColumn;
    @FXML private ComboBox<String> filterCombo;
    @FXML private TextField searchField;
    
    private ObservableList<BookCopy> bookCopyList = FXCollections.observableArrayList();
    private ObservableList<BookCopy> allBookCopies = FXCollections.observableArrayList();
    private BookCopyDao bookCopyDao = new BookCopyDaoImpl();
    private BookDao bookDao = new BookDaoImpl();
    
    @FXML
    public void initialize() {
        // Set up table columns
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        isbnColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBook().getIsbn()));
        bookTitleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Due date column - show due date if borrowed
        dueDateColumn.setCellValueFactory(cellData -> {
            BookCopy copy = cellData.getValue();
            if (copy.getStatus() == BookCopy.BookStatus.BORROWED && copy.getCurrentLoan() != null) {
                return new SimpleStringProperty(copy.getCurrentLoan().getDueDate().toString());
            }
            return new SimpleStringProperty("-");
        });
        
        // Set up filter combo
        filterCombo.getItems().addAll("All Copies", "Available Only", "Borrowed Only");
        filterCombo.setValue("All Copies");
        filterCombo.setOnAction(e -> applyFilters());
        
        bookCopyTable.setItems(bookCopyList);
        loadBookCopies();
    }
    
    private void loadBookCopies() {
        allBookCopies.clear();
        allBookCopies.addAll(bookCopyDao.findAll());
        applyFilters();
    }
    
    private void applyFilters() {
        bookCopyList.clear();
        
        String filter = filterCombo.getValue();
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        List<BookCopy> filtered = allBookCopies.stream()
            .filter(copy -> {
                // Apply status filter
                if ("Available Only".equals(filter) && copy.getStatus() != BookCopy.BookStatus.AVAILABLE) {
                    return false;
                }
                if ("Borrowed Only".equals(filter) && copy.getStatus() != BookCopy.BookStatus.BORROWED) {
                    return false;
                }
                
                // Apply search filter
                if (!searchTerm.isEmpty()) {
                    return copy.getBook().getIsbn().toLowerCase().contains(searchTerm) ||
                           copy.getLocation().toLowerCase().contains(searchTerm);
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        bookCopyList.addAll(filtered);
    }
    
    @FXML
    private void handleSearch() {
        applyFilters();
    }
    
    @FXML
    private void handleClear() {
        searchField.clear();
        filterCombo.setValue("All Copies");
        applyFilters();
    }
    
    @FXML
    private void handleAdd() {
        Dialog<BookCopy> dialog = createBookCopyDialog(null);
        Optional<BookCopy> result = dialog.showAndWait();
        
        result.ifPresent(bookCopy -> {
            try {
                bookCopyDao.save(bookCopy);
                loadBookCopies();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book copy added successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add book copy: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleEdit() {
        BookCopy selected = bookCopyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book copy to edit.");
            return;
        }
        
        Dialog<BookCopy> dialog = createBookCopyDialog(selected);
        Optional<BookCopy> result = dialog.showAndWait();
        
        result.ifPresent(bookCopy -> {
            try {
                bookCopyDao.update(bookCopy);
                loadBookCopies();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book copy updated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book copy: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleDelete() {
        BookCopy selected = bookCopyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book copy to delete.");
            return;
        }
        
        if (selected.getStatus() == BookCopy.BookStatus.BORROWED) {
            showAlert(Alert.AlertType.WARNING, "Cannot Delete", "Cannot delete a borrowed book copy.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Book Copy");
        alert.setContentText("Are you sure you want to delete book copy: " + selected.getBarcode() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookCopyDao.delete(selected);
                    loadBookCopies();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book copy deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book copy: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadBookCopies();
    }
    
    private Dialog<BookCopy> createBookCopyDialog(BookCopy bookCopy) {
        Dialog<BookCopy> dialog = new Dialog<>();
        dialog.setTitle(bookCopy == null ? "Add Book Copy" : "Edit Book Copy");
        dialog.setHeaderText(bookCopy == null ? "Enter book copy details:" : "Edit book copy details:");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField barcodeField = new TextField(bookCopy == null ? "" : bookCopy.getBarcode());
        ComboBox<Book> bookCombo = new ComboBox<>();
        TextField locationField = new TextField(bookCopy == null ? "" : bookCopy.getLocation());
        
        // Load books into combo
        List<Book> books = bookDao.findAll();
        bookCombo.setItems(FXCollections.observableArrayList(books));
        bookCombo.setConverter(new StringConverter<Book>() {
            @Override
            public String toString(Book book) {
                return book != null ? book.getTitle() + " (ISBN: " + book.getIsbn() + ")" : "";
            }
            
            @Override
            public Book fromString(String string) {
                return null;
            }
        });
        
        if (bookCopy != null) {
            barcodeField.setDisable(true);
            bookCombo.setValue(bookCopy.getBook());
            bookCombo.setDisable(true);
        }
        
        grid.add(new Label("Barcode:"), 0, 0);
        grid.add(barcodeField, 1, 0);
        grid.add(new Label("Book:"), 0, 1);
        grid.add(bookCombo, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (bookCombo.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a book!");
                    return null;
                }
                
                if (bookCopy == null) {
                    return new BookCopy(
                        barcodeField.getText().trim(),
                        locationField.getText().trim(),
                        bookCombo.getValue()
                    );
                } else {
                    bookCopy.setLocation(locationField.getText().trim());
                    return bookCopy;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 