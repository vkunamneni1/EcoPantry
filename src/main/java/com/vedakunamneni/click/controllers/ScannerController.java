package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ScannerController {

    @FXML
    private VBox dropZone;

    @FXML
    private VBox ingredientsBox;

    @FXML
    private VBox ingredientsSection;

    @FXML
    private Button addSelectedButton;

    private List<CheckBox> ingredientCheckBoxes = new ArrayList<>();

    @FXML
    public void initialize() {
        dropZone.setOnDragOver(e -> {
            if (e.getGestureSource() != dropZone && e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        dropZone.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                processReceipt();
            }
            e.setDropCompleted(success);
            e.consume();
        });

        dropZone.setOnMouseClicked(e -> {
            processReceipt(); // Simulated upload click
        });
    }

    private void processReceipt() {
        // For now, show mock ingredients - later this would integrate with OCR
        showDetectedIngredients();
    }

    private void showDetectedIngredients() {
        ingredientsBox.getChildren().clear();
        ingredientCheckBoxes.clear();
        
        // Mock detected ingredients from receipt
        String[] detectedItems = {
            "Organic Carrots", "Roma Tomatoes", "Fresh Spinach", "Broccoli Crowns", 
            "Yellow Onions", "Garlic Bulbs", "Zucchini", "Bell Peppers", 
            "Green Cabbage", "Baby Kale", "Celery Stalks", "Sweet Potatoes", 
            "White Mushrooms", "Bananas", "Apples", "Whole Milk", "Large Eggs", 
            "Cheddar Cheese", "Whole Wheat Bread", "Chicken Breast"
        };
        
        for (String item : detectedItems) {
            HBox itemBox = new HBox(10);
            itemBox.setStyle("-fx-alignment: center-left; -fx-padding: 5;");
            
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(true); // Default to selected
            
            Label label = new Label("🥬 " + item);
            label.setStyle("-fx-font-size: 14px;");
            
            itemBox.getChildren().addAll(checkBox, label);
            ingredientsBox.getChildren().add(itemBox);
            ingredientCheckBoxes.add(checkBox);
        }
        
        // Show ingredients section
        ingredientsSection.setVisible(true);
        ingredientsSection.setManaged(true);
        
        // Update drop zone text
        dropZone.getChildren().clear();
        Label successLabel = new Label("✅ Receipt processed successfully!");
        successLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: green;");
        dropZone.getChildren().add(successLabel);
    }

    @FXML
    private void addSelectedToInventory() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "No user logged in!");
            return;
        }

        List<String> selectedIngredients = new ArrayList<>();
        for (int i = 0; i < ingredientCheckBoxes.size(); i++) {
            CheckBox checkBox = ingredientCheckBoxes.get(i);
            if (checkBox.isSelected()) {
                HBox itemBox = (HBox) ingredientsBox.getChildren().get(i);
                Label label = (Label) itemBox.getChildren().get(1);
                String ingredientText = label.getText();
                // Remove emoji and extract ingredient name
                String ingredientName = ingredientText.substring(ingredientText.indexOf(" ") + 1).trim();
                selectedIngredients.add(ingredientName);
            }
        }

        if (selectedIngredients.isEmpty()) {
            showAlert("Warning", "Please select at least one ingredient to add!");
            return;
        }

        // Add selected ingredients to inventory
        int successCount = 0;
        for (String ingredient : selectedIngredients) {
            if (DatabaseHelper.addIngredientToInventory(currentUser, ingredient, 1)) {
                successCount++;
            }
        }

        if (successCount > 0) {
            showAlert("Success", "Added " + successCount + " ingredient(s) to your inventory!");
            // Reset the scanner
            resetScanner();
        } else {
            showAlert("Error", "Failed to add ingredients to inventory!");
        }
    }

    @FXML
    private void selectAllIngredients() {
        for (CheckBox checkBox : ingredientCheckBoxes) {
            checkBox.setSelected(true);
        }
    }

    @FXML
    private void clearAllIngredients() {
        for (CheckBox checkBox : ingredientCheckBoxes) {
            checkBox.setSelected(false);
        }
    }

    private void resetScanner() {
        ingredientsSection.setVisible(false);
        ingredientsSection.setManaged(false);
        dropZone.getChildren().clear();
        Label defaultLabel = new Label("Drag and Drop a file here or click to upload");
        defaultLabel.getStyleClass().add("drop-text");
        dropZone.getChildren().add(defaultLabel);
        ingredientCheckBoxes.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 🔁 Navigation actions
    @FXML private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML private void goToInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML private void handleLogout() throws IOException {
        App.setRoot("start");
    }
}
