package com.cs3560.library.ui;

import com.cs3560.library.dao.BookDao;
import com.cs3560.library.dao.BookDaoImpl;
import com.cs3560.library.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.util.Optional;

public class BookController {
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorsColumn;
    @FXML private TableColumn<Book, String> publisherColumn;
    @FXML private TableColumn<Book, LocalDate> pubDateColumn;
    @FXML private TableColumn<Book, Integer> pagesColumn;
    @FXML private TextField searchField;
    
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private BookDao bookDao = new BookDaoImpl();
    
    @FXML
    public void initialize() {
        // Set up table columns
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorsColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        pubDateColumn.setCellValueFactory(new PropertyValueFactory<>("pubDate"));
        pagesColumn.setCellValueFactory(new PropertyValueFactory<>("pages"));
        
        bookTable.setItems(bookList);
        loadBooks();
    }
    
    private void loadBooks() {
        bookList.clear();
        bookList.addAll(bookDao.findAll());
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadBooks();
        } else {
            bookList.clear();
            bookList.addAll(bookDao.searchByTitleOrAuthorOrIsbn(searchTerm));
        }
    }
    
    @FXML
    private void handleClear() {
        searchField.clear();
        loadBooks();
    }
    
    @FXML
    private void handleAdd() {
        Dialog<Book> dialog = createBookDialog(null);
        Optional<Book> result = dialog.showAndWait();
        
        result.ifPresent(book -> {
            try {
                bookDao.save(book);
                loadBooks();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book added successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add book: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleEdit() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to edit.");
            return;
        }
        
        Dialog<Book> dialog = createBookDialog(selected);
        Optional<Book> result = dialog.showAndWait();
        
        result.ifPresent(book -> {
            try {
                bookDao.update(book);
                loadBooks();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book updated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleDelete() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Book");
        alert.setContentText("Are you sure you want to delete book: " + selected.getTitle() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookDao.delete(selected);
                    loadBooks();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete book: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleViewDetails() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a book to view details.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Details");
        alert.setHeaderText(selected.getTitle());
        alert.setContentText(
            "ISBN: " + selected.getIsbn() + "\n" +
            "Authors: " + selected.getAuthors() + "\n" +
            "Publisher: " + selected.getPublisher() + "\n" +
            "Publication Date: " + selected.getPubDate() + "\n" +
            "Pages: " + selected.getPages() + "\n\n" +
            "Description:\n" + (selected.getDescription() != null ? selected.getDescription() : "No description available")
        );
        alert.showAndWait();
    }
    
    @FXML
    private void handleRefresh() {
        loadBooks();
    }
    
    private Dialog<Book> createBookDialog(Book book) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(book == null ? "Add Book" : "Edit Book");
        dialog.setHeaderText(book == null ? "Enter book details:" : "Edit book details:");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField isbnField = new TextField(book == null ? "" : book.getIsbn());
        TextField titleField = new TextField(book == null ? "" : book.getTitle());
        TextField authorsField = new TextField(book == null ? "" : book.getAuthors());
        TextField publisherField = new TextField(book == null ? "" : book.getPublisher());
        TextField pagesField = new TextField(book == null ? "" : String.valueOf(book.getPages()));
        DatePicker pubDatePicker = new DatePicker(book == null ? LocalDate.now() : book.getPubDate());
        TextArea descriptionArea = new TextArea(book == null ? "" : book.getDescription());
        descriptionArea.setPrefRowCount(3);
        
        // Disable ISBN field when editing
        if (book != null) {
            isbnField.setDisable(true);
        }
        
        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Authors:"), 0, 2);
        grid.add(authorsField, 1, 2);
        grid.add(new Label("Publisher:"), 0, 3);
        grid.add(publisherField, 1, 3);
        grid.add(new Label("Publication Date:"), 0, 4);
        grid.add(pubDatePicker, 1, 4);
        grid.add(new Label("Pages:"), 0, 5);
        grid.add(pagesField, 1, 5);
        grid.add(new Label("Description:"), 0, 6);
        grid.add(descriptionArea, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int pages = Integer.parseInt(pagesField.getText().trim());
                    
                    if (book == null) {
                        return new Book(
                            isbnField.getText().trim(),
                            titleField.getText().trim(),
                            descriptionArea.getText().trim(),
                            authorsField.getText().trim(),
                            pages,
                            publisherField.getText().trim(),
                            pubDatePicker.getValue()
                        );
                    } else {
                        book.setTitle(titleField.getText().trim());
                        book.setDescription(descriptionArea.getText().trim());
                        book.setAuthors(authorsField.getText().trim());
                        book.setPages(pages);
                        book.setPublisher(publisherField.getText().trim());
                        book.setPubDate(pubDatePicker.getValue());
                        return book;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Pages must be a number!");
                    return null;
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