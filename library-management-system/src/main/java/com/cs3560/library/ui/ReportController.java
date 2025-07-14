package com.cs3560.library.ui;

import com.cs3560.library.dao.StudentDao;
import com.cs3560.library.dao.StudentDaoImpl;
import com.cs3560.library.model.Loan;
import com.cs3560.library.model.Student;
import com.cs3560.library.service.LibraryService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class ReportController {
    @FXML private ComboBox<Student> studentCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label reportSummaryLabel;
    @FXML private Label statsLabel;
    
    @FXML private TableView<Loan> reportTable;
    @FXML private TableColumn<Loan, Long> loanIdColumn;
    @FXML private TableColumn<Loan, String> studentColumn;
    @FXML private TableColumn<Loan, Integer> bookCountColumn;
    @FXML private TableColumn<Loan, LocalDate> borrowDateColumn;
    @FXML private TableColumn<Loan, LocalDate> dueDateColumn;
    @FXML private TableColumn<Loan, String> returnDateColumn;
    @FXML private TableColumn<Loan, String> statusColumn;
    @FXML private TableColumn<Loan, Long> daysLateColumn;
    
    private ObservableList<Loan> reportData = FXCollections.observableArrayList();
    private ObservableList<Loan> allLoans = FXCollections.observableArrayList();
    private LibraryService libraryService = new LibraryService();
    private StudentDao studentDao = new StudentDaoImpl();
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupStudentCombo();
        setupDatePickers();
        
        filterCombo.getItems().addAll("All Loans", "Active Only", "Returned Only", "Overdue Only");
        filterCombo.setValue("All Loans");
        reportTable.setItems(reportData);
    }
    
    private void setupTableColumns() {
        loanIdColumn.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        studentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudent().getName()));
        bookCountColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getBookCopies().size()).asObject());
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        
        returnDateColumn.setCellValueFactory(cellData -> {
            LocalDate returnDate = cellData.getValue().getReturnDate();
            return new SimpleStringProperty(returnDate != null ? returnDate.toString() : "-");
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            Loan loan = cellData.getValue();
            String status;
            if (loan.isReturned()) {
                status = "Returned";
            } else if (loan.isOverdue()) {
                status = "Overdue";
            } else {
                status = "Active";
            }
            return new SimpleStringProperty(status);
        });
        
        daysLateColumn.setCellValueFactory(cellData -> {
            Loan loan = cellData.getValue();
            long daysLate = 0;
            if (loan.isOverdue() && !loan.isReturned()) {
                daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
            } else if (loan.isReturned() && loan.getReturnDate().isAfter(loan.getDueDate())) {
                daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());
            }
            return new SimpleLongProperty(daysLate).asObject();
        });
        
        // Format days late column
        daysLateColumn.setCellFactory(column -> new TableCell<Loan, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty || item == 0) {
                    setText("-");
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: red;");
                }
            }
        });
        
        // Style status column
        statusColumn.setCellFactory(column -> new TableCell<Loan, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Overdue":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "Active":
                            setStyle("-fx-text-fill: green;");
                            break;
                        case "Returned":
                            setStyle("-fx-text-fill: gray;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    private void setupStudentCombo() {
        List<Student> students = studentDao.findAll();
        studentCombo.setItems(FXCollections.observableArrayList(students));
        
        studentCombo.setConverter(new StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student != null ? student.getName() + " (" + student.getBroncoId() + ")" : "All Students";
            }
            
            @Override
            public Student fromString(String string) {
                return null;
            }
        });
        
        // Add "All Students" option
        studentCombo.setPromptText("All Students");
    }
    
    private void setupDatePickers() {
        // Default to last 30 days
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }
    
    @FXML
    private void generateReport() {
        Student selectedStudent = studentCombo.getValue();
        String studentId = selectedStudent != null ? selectedStudent.getBroncoId() : null;
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Dates", "Please select both start and end dates.");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Dates", "Start date must be before end date.");
            return;
        }
        
        // Get loans from service
        List<Loan> loans = libraryService.generateReport(studentId, startDate, endDate);
        allLoans.clear();
        allLoans.addAll(loans);
        
        applyFilter();
        updateStatistics();
    }
    
    private void applyFilter() {
        String filter = filterCombo.getValue();
        List<Loan> filtered = allLoans.stream()
            .filter(loan -> {
                switch (filter) {
                    case "Active Only":
                        return !loan.isReturned();
                    case "Returned Only":
                        return loan.isReturned();
                    case "Overdue Only":
                        return loan.isOverdue() || 
                               (loan.isReturned() && loan.getReturnDate().isAfter(loan.getDueDate()));
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
        
        reportData.clear();
        reportData.addAll(filtered);
        
        reportSummaryLabel.setText("Report Summary: " + filtered.size() + " loans found");
    }
    
    private void updateStatistics() {
        int total = allLoans.size();
        long active = allLoans.stream().filter(l -> !l.isReturned()).count();
        long overdue = allLoans.stream().filter(Loan::isOverdue).count();
        long returned = allLoans.stream().filter(Loan::isReturned).count();
        
        statsLabel.setText(String.format("Total Loans: %d | Active: %d | Overdue: %d | Returned: %d",
                                        total, active, overdue, returned));
    }
    
    @FXML
    private void clearFilters() {
        studentCombo.setValue(null);
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        filterCombo.setValue("All Loans");
        reportData.clear();
        allLoans.clear();
        reportSummaryLabel.setText("Report Summary: 0 loans found");
        statsLabel.setText("Total Loans: 0 | Active: 0 | Overdue: 0 | Returned: 0");
    }
    
    @FXML
    private void exportToCSV() {
        if (reportData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No data to export. Please generate a report first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("loan_report_" + LocalDate.now() + ".csv");
        
        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                writer.println("Loan ID,Student ID,Student Name,Book Count,Borrow Date,Due Date,Return Date,Status,Days Late");
                
                // Write data
                for (Loan loan : reportData) {
                    String status;
                    if (loan.isReturned()) {
                        status = "Returned";
                    } else if (loan.isOverdue()) {
                        status = "Overdue";
                    } else {
                        status = "Active";
                    }
                    
                    long daysLate = 0;
                    if (loan.isOverdue() && !loan.isReturned()) {
                        daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                    } else if (loan.isReturned() && loan.getReturnDate().isAfter(loan.getDueDate())) {
                        daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());
                    }
                    
                    writer.printf("%d,%s,%s,%d,%s,%s,%s,%s,%s%n",
                        loan.getLoanId(),
                        loan.getStudent().getBroncoId(),
                        loan.getStudent().getName(),
                        loan.getBookCopies().size(),
                        loan.getBorrowDate(),
                        loan.getDueDate(),
                        loan.getReturnDate() != null ? loan.getReturnDate() : "",
                        status,
                        daysLate > 0 ? daysLate : ""
                    );
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                         "Report exported successfully to:\n" + file.getAbsolutePath());
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", 
                         "Failed to export report: " + e.getMessage());
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 