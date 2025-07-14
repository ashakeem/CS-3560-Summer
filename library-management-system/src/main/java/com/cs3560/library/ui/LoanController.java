package com.cs3560.library.ui;

import com.cs3560.library.model.Loan;
import com.cs3560.library.service.LibraryService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.List;

public class LoanController {
    @FXML private ComboBox<Loan> activeLoanCombo;
    @FXML private TableView<Loan> activeLoansTable;
    @FXML private TableColumn<Loan, Long> loanIdColumn;
    @FXML private TableColumn<Loan, String> studentColumn;
    @FXML private TableColumn<Loan, LocalDate> borrowDateColumn;
    @FXML private TableColumn<Loan, LocalDate> dueDateColumn;
    @FXML private TableColumn<Loan, Integer> itemCountColumn;
    @FXML private TableColumn<Loan, String> overdueColumn;
    
    private ObservableList<Loan> activeLoansList = FXCollections.observableArrayList();
    private LibraryService libraryService = new LibraryService();
    
    @FXML
    public void initialize() {
        // Set up table columns
        loanIdColumn.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        studentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudent().getName() + 
                                   " (" + cellData.getValue().getStudent().getBroncoId() + ")"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        itemCountColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getBookCopies().size()).asObject());
        overdueColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isOverdue() ? "OVERDUE" : "Active"));
        
        // Color overdue loans
        overdueColumn.setCellFactory(column -> new TableCell<Loan, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("OVERDUE".equals(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
        
        // Set up combo box
        activeLoanCombo.setConverter(new StringConverter<Loan>() {
            @Override
            public String toString(Loan loan) {
                if (loan == null) return "";
                return "Loan #" + loan.getLoanId() + " - " + loan.getStudent().getName() + 
                       " - " + loan.getBookCopies().size() + " items";
            }
            
            @Override
            public Loan fromString(String string) {
                return null;
            }
        });
        
        activeLoansTable.setItems(activeLoansList);
        loadActiveLoans();
    }
    
    private void loadActiveLoans() {
        List<Loan> activeLoans = libraryService.getActiveLoans();
        activeLoansList.clear();
        activeLoansList.addAll(activeLoans);
        
        activeLoanCombo.setItems(FXCollections.observableArrayList(activeLoans));
        if (!activeLoans.isEmpty()) {
            activeLoanCombo.setValue(activeLoans.get(0));
        }
    }
    
    @FXML
    private void handleReturnLoan() {
        Loan selectedLoan = activeLoanCombo.getValue();
        if (selectedLoan == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a loan to return.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Return");
        confirmAlert.setHeaderText("Return Loan");
        confirmAlert.setContentText("Are you sure you want to return all " + 
                                   selectedLoan.getBookCopies().size() + 
                                   " items in loan #" + selectedLoan.getLoanId() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    libraryService.returnLoan(selectedLoan.getLoanId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                             "Loan returned successfully!\n\nLoan ID: " + selectedLoan.getLoanId() +
                             "\nStudent: " + selectedLoan.getStudent().getName() +
                             "\nItems returned: " + selectedLoan.getBookCopies().size());
                    loadActiveLoans();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                             "Failed to return loan: " + e.getMessage());
                }
            }
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 