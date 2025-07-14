package com.cs3560.library.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

public class MainController {
    
    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("CS3560 Library Management System");
        alert.setContentText("Version 1.0\n\nA comprehensive library management system for CS3560.\n\n" +
                           "Features:\n" +
                           "- Student Management\n" +
                           "- Book Catalog Management\n" +
                           "- Book Copy Tracking\n" +
                           "- Loan Management\n" +
                           "- Report Generation");
        alert.showAndWait();
    }
    
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }
} 