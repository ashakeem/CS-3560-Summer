package com.cs3560.library.ui;

import com.cs3560.library.dao.StudentDao;
import com.cs3560.library.dao.StudentDaoImpl;
import com.cs3560.library.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import java.io.IOException;
import java.util.Optional;

public class StudentController {
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> broncoIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> addressColumn;
    @FXML private TableColumn<Student, String> degreeColumn;
    @FXML private TextField searchField;
    
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private StudentDao studentDao = new StudentDaoImpl();
    
    @FXML
    public void initialize() {
        // Set up table columns
        broncoIdColumn.setCellValueFactory(new PropertyValueFactory<>("broncoId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        degreeColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));
        
        studentTable.setItems(studentList);
        loadStudents();
    }
    
    private void loadStudents() {
        studentList.clear();
        studentList.addAll(studentDao.findAll());
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadStudents();
        } else {
            studentList.clear();
            studentList.addAll(studentDao.findByName(searchTerm));
        }
    }
    
    @FXML
    private void handleClear() {
        searchField.clear();
        loadStudents();
    }
    
    @FXML
    private void handleAdd() {
        Dialog<Student> dialog = createStudentDialog(null);
        Optional<Student> result = dialog.showAndWait();
        
        result.ifPresent(student -> {
            try {
                studentDao.save(student);
                loadStudents();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student added successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add student: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleEdit() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit.");
            return;
        }
        
        Dialog<Student> dialog = createStudentDialog(selected);
        Optional<Student> result = dialog.showAndWait();
        
        result.ifPresent(student -> {
            try {
                studentDao.update(student);
                loadStudents();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student updated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update student: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Student");
        alert.setContentText("Are you sure you want to delete student: " + selected.getName() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    studentDao.delete(selected);
                    loadStudents();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadStudents();
    }
    
    private Dialog<Student> createStudentDialog(Student student) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Add Student" : "Edit Student");
        dialog.setHeaderText(student == null ? "Enter student details:" : "Edit student details:");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField broncoIdField = new TextField(student == null ? "" : student.getBroncoId());
        TextField nameField = new TextField(student == null ? "" : student.getName());
        TextField addressField = new TextField(student == null ? "" : student.getAddress());
        TextField degreeField = new TextField(student == null ? "" : student.getDegree());
        
        // Disable bronco ID field when editing
        if (student != null) {
            broncoIdField.setDisable(true);
        }
        
        grid.add(new Label("Bronco ID:"), 0, 0);
        grid.add(broncoIdField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Degree:"), 0, 3);
        grid.add(degreeField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (student == null) {
                    return new Student(
                        broncoIdField.getText().trim(),
                        nameField.getText().trim(),
                        addressField.getText().trim(),
                        degreeField.getText().trim()
                    );
                } else {
                    student.setName(nameField.getText().trim());
                    student.setAddress(addressField.getText().trim());
                    student.setDegree(degreeField.getText().trim());
                    return student;
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