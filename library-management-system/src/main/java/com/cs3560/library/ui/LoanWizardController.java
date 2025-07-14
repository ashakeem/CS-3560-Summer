package com.cs3560.library.ui;

import com.cs3560.library.dao.StudentDao;
import com.cs3560.library.dao.StudentDaoImpl;
import com.cs3560.library.model.*;
import com.cs3560.library.service.LibraryService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LoanWizardController {
    @FXML private StackPane wizardStack;
    @FXML private VBox step1, step2, step3, step4;
    
    // Step 1 controls
    @FXML private TextField studentSearchField;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> studentNameColumn;
    @FXML private TableColumn<Student, String> studentDegreeColumn;
    
    // Step 2 controls
    @FXML private TextField bookSearchField;
    @FXML private TableView<LibraryService.BookSearchResult> bookSearchTable;
    @FXML private TableColumn<LibraryService.BookSearchResult, String> bookIsbnColumn;
    @FXML private TableColumn<LibraryService.BookSearchResult, String> bookTitleColumn;
    @FXML private TableColumn<LibraryService.BookSearchResult, String> bookAuthorsColumn;
    @FXML private TableColumn<LibraryService.BookSearchResult, Integer> availableCopiesColumn;
    @FXML private TableView<BookCopySelection> availableCopiesTable;
    @FXML private TableColumn<BookCopySelection, String> copyBarcodeColumn;
    @FXML private TableColumn<BookCopySelection, String> copyLocationColumn;
    @FXML private TableColumn<BookCopySelection, Boolean> copySelectColumn;
    @FXML private Label selectedCopiesLabel;
    
    // Step 3 controls
    @FXML private DatePicker dueDatePicker;
    @FXML private TextArea loanSummaryArea;
    
    // Step 4 controls
    @FXML private TextArea receiptArea;
    
    // Navigation buttons
    @FXML private Button previousButton, nextButton, cancelButton;
    
    private int currentStep = 1;
    private Student selectedStudent;
    private ObservableList<BookCopySelection> bookCopySelections = FXCollections.observableArrayList();
    private List<String> selectedBarcodes = new ArrayList<>();
    private Loan createdLoan;
    
    private LibraryService libraryService = new LibraryService();
    private StudentDao studentDao = new StudentDaoImpl();
    
    @FXML
    public void initialize() {
        setupStep1();
        setupStep2();
        setupStep3();
        updateNavigationButtons();
    }
    
    private void setupStep1() {
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("broncoId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentDegreeColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));
        
        loadAllStudents();
    }
    
    private void setupStep2() {
        // Book search table
        bookIsbnColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBook().getIsbn()));
        bookTitleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        bookAuthorsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBook().getAuthors()));
        availableCopiesColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getAvailableCopies()).asObject());
        
        // Available copies table
        copyBarcodeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().barcodeProperty());
        copyLocationColumn.setCellValueFactory(cellData -> 
            cellData.getValue().locationProperty());
        copySelectColumn.setCellValueFactory(cellData -> 
            cellData.getValue().selectedProperty());
        copySelectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(copySelectColumn));
        
        availableCopiesTable.setItems(bookCopySelections);
        availableCopiesTable.setEditable(true);
        
        // Listen for book selection
        bookSearchTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAvailableCopies(newVal);
            }
        });
    }
    
    private void setupStep3() {
        LocalDate today = LocalDate.now();
        dueDatePicker.setValue(today.plusDays(14)); // Default 2 weeks
        dueDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate maxDate = today.plusDays(180);
                setDisable(empty || date.isBefore(today) || date.isAfter(maxDate));
            }
        });
    }
    
    @FXML
    private void searchStudents() {
        String searchTerm = studentSearchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadAllStudents();
        } else {
            List<Student> results = studentDao.findByName(searchTerm);
            studentTable.setItems(FXCollections.observableArrayList(results));
        }
    }
    
    private void loadAllStudents() {
        List<Student> allStudents = studentDao.findAll();
        studentTable.setItems(FXCollections.observableArrayList(allStudents));
    }
    
    @FXML
    private void searchBooks() {
        String searchTerm = bookSearchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<LibraryService.BookSearchResult> results = libraryService.searchBooks(searchTerm);
            bookSearchTable.setItems(FXCollections.observableArrayList(results));
        }
    }
    
    private void loadAvailableCopies(LibraryService.BookSearchResult bookResult) {
        bookCopySelections.clear();
        
        for (BookCopy copy : bookResult.getAvailableCopiesList()) {
            BookCopySelection selection = new BookCopySelection(copy);
            selection.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateSelectedCount();
            });
            bookCopySelections.add(selection);
        }
    }
    
    private void updateSelectedCount() {
        long count = bookCopySelections.stream().filter(BookCopySelection::isSelected).count();
        selectedCopiesLabel.setText("Selected Copies: " + count);
    }
    
    @FXML
    private void nextStep() {
        if (validateCurrentStep()) {
            if (currentStep == 3) {
                // Create the loan
                createLoan();
            } else {
                currentStep++;
                showStep(currentStep);
                updateNavigationButtons();
                
                if (currentStep == 3) {
                    updateLoanSummary();
                }
            }
        }
    }
    
    @FXML
    private void previousStep() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
            updateNavigationButtons();
        }
    }
    
    @FXML
    private void cancel() {
        resetWizard();
    }
    
    @FXML
    private void printReceipt() {
        showAlert(Alert.AlertType.INFORMATION, "Print", "Receipt sent to printer (simulated).");
    }
    
    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                selectedStudent = studentTable.getSelectionModel().getSelectedItem();
                if (selectedStudent == null) {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student.");
                    return false;
                }
                
                // Check for overdue loans
                if (studentDao.hasOverdueLoans(selectedStudent.getBroncoId())) {
                    showAlert(Alert.AlertType.ERROR, "Cannot Proceed", 
                             "Student has overdue loans and cannot borrow more books.");
                    return false;
                }
                
                // Check active loan count
                int activeLoans = studentDao.countActiveLoans(selectedStudent.getBroncoId());
                if (activeLoans >= 5) {
                    showAlert(Alert.AlertType.ERROR, "Cannot Proceed", 
                             "Student already has 5 active loans.");
                    return false;
                }
                return true;
                
            case 2:
                selectedBarcodes = bookCopySelections.stream()
                    .filter(BookCopySelection::isSelected)
                    .map(s -> s.getBookCopy().getBarcode())
                    .collect(Collectors.toList());
                    
                if (selectedBarcodes.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "No Selection", 
                             "Please select at least one book copy to borrow.");
                    return false;
                }
                return true;
                
            case 3:
                if (dueDatePicker.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Date", "Please select a due date.");
                    return false;
                }
                return true;
                
            default:
                return true;
        }
    }
    
    private void updateLoanSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("LOAN SUMMARY\n");
        summary.append("============\n\n");
        summary.append("Student: ").append(selectedStudent.getName())
               .append(" (").append(selectedStudent.getBroncoId()).append(")\n");
        summary.append("Degree: ").append(selectedStudent.getDegree()).append("\n\n");
        summary.append("Items to borrow (").append(selectedBarcodes.size()).append("):\n");
        summary.append("-------------------\n");
        
        for (BookCopySelection selection : bookCopySelections) {
            if (selection.isSelected()) {
                BookCopy copy = selection.getBookCopy();
                summary.append("• ").append(copy.getBook().getTitle())
                       .append("\n  Barcode: ").append(copy.getBarcode())
                       .append(", Location: ").append(copy.getLocation())
                       .append("\n");
            }
        }
        
        summary.append("\nBorrow Date: ").append(LocalDate.now()).append("\n");
        summary.append("Due Date: ").append(dueDatePicker.getValue()).append("\n");
        
        loanSummaryArea.setText(summary.toString());
    }
    
    private void createLoan() {
        try {
            createdLoan = libraryService.createLoan(
                selectedStudent.getBroncoId(),
                selectedBarcodes,
                dueDatePicker.getValue()
            );
            
            currentStep = 4;
            showStep(4);
            updateNavigationButtons();
            generateReceipt();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create loan: " + e.getMessage());
        }
    }
    
    private void generateReceipt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("     CS3560 LIBRARY - LOAN RECEIPT      \n");
        receipt.append("========================================\n\n");
        receipt.append("Loan ID: #").append(createdLoan.getLoanId()).append("\n");
        receipt.append("Date: ").append(LocalDate.now().format(formatter)).append("\n\n");
        receipt.append("STUDENT INFORMATION:\n");
        receipt.append("-------------------\n");
        receipt.append("Name: ").append(selectedStudent.getName()).append("\n");
        receipt.append("ID: ").append(selectedStudent.getBroncoId()).append("\n");
        receipt.append("Degree: ").append(selectedStudent.getDegree()).append("\n\n");
        receipt.append("BORROWED ITEMS:\n");
        receipt.append("---------------\n");
        
        for (BookCopy copy : createdLoan.getBookCopies()) {
            receipt.append("• ").append(copy.getBook().getTitle()).append("\n");
            receipt.append("  ISBN: ").append(copy.getBook().getIsbn()).append("\n");
            receipt.append("  Barcode: ").append(copy.getBarcode()).append("\n");
            receipt.append("  Location: ").append(copy.getLocation()).append("\n\n");
        }
        
        receipt.append("LOAN DETAILS:\n");
        receipt.append("-------------\n");
        receipt.append("Borrow Date: ").append(createdLoan.getBorrowDate()).append("\n");
        receipt.append("Due Date: ").append(createdLoan.getDueDate()).append("\n");
        receipt.append("Total Items: ").append(createdLoan.getBookCopies().size()).append("\n\n");
        receipt.append("========================================\n");
        receipt.append("Please return all items by the due date.\n");
        receipt.append("Late returns may incur penalties.\n");
        receipt.append("========================================\n");
        
        receiptArea.setText(receipt.toString());
    }
    
    private void showStep(int step) {
        step1.setVisible(step == 1);
        step2.setVisible(step == 2);
        step3.setVisible(step == 3);
        step4.setVisible(step == 4);
    }
    
    private void updateNavigationButtons() {
        previousButton.setDisable(currentStep == 1 || currentStep == 4);
        
        if (currentStep == 3) {
            nextButton.setText("Create Loan");
        } else if (currentStep == 4) {
            nextButton.setText("New Loan");
        } else {
            nextButton.setText("Next");
        }
        
        nextButton.setDisable(currentStep == 4);
        cancelButton.setText(currentStep == 4 ? "Close" : "Cancel");
    }
    
    private void resetWizard() {
        currentStep = 1;
        showStep(1);
        updateNavigationButtons();
        
        selectedStudent = null;
        selectedBarcodes.clear();
        bookCopySelections.clear();
        createdLoan = null;
        
        studentSearchField.clear();
        bookSearchField.clear();
        bookSearchTable.getItems().clear();
        dueDatePicker.setValue(LocalDate.now().plusDays(14));
        loanSummaryArea.clear();
        receiptArea.clear();
        
        loadAllStudents();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Inner class for book copy selection
    public static class BookCopySelection {
        private final BookCopy bookCopy;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        
        public BookCopySelection(BookCopy bookCopy) {
            this.bookCopy = bookCopy;
        }
        
        public BookCopy getBookCopy() {
            return bookCopy;
        }
        
        public boolean isSelected() {
            return selected.get();
        }
        
        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
        
        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }
        
        public SimpleStringProperty barcodeProperty() {
            return new SimpleStringProperty(bookCopy.getBarcode());
        }
        
        public SimpleStringProperty locationProperty() {
            return new SimpleStringProperty(bookCopy.getLocation());
        }
    }
} 