package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    private Map<CheckBox, DatePicker> ingredientDatePickers = new HashMap<>();
    private Map<CheckBox, TextField> ingredientQuantityFields = new HashMap<>();

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
        ingredientDatePickers.clear();
        ingredientQuantityFields.clear();
        
        // Mock detected ingredients from receipt
        String[] detectedItems = {
            "Organic Carrots", "Roma Tomatoes", "Fresh Spinach", "Broccoli Crowns", 
            "Yellow Onions", "Garlic Bulbs", "Zucchini", "Bell Peppers", 
            "Green Cabbage", "Baby Kale", "Celery Stalks", "Sweet Potatoes", 
            "White Mushrooms", "Bananas", "Apples", "Whole Milk", "Large Eggs", 
            "Cheddar Cheese", "Whole Wheat Bread", "Chicken Breast"
        };
        
        for (String item : detectedItems) {
            VBox itemBox = new VBox(5);
            itemBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
            
            // Top row with checkbox and item name
            HBox topRow = new HBox(10);
            topRow.setStyle("-fx-alignment: center-left;");
            
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(true); // Default to selected
            
            Label label = new Label(getIngredientEmoji(item) + " " + item);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            topRow.getChildren().addAll(checkBox, label);
            
            // Bottom row with quantity and expiration date
            HBox bottomRow = new HBox(15);
            bottomRow.setStyle("-fx-alignment: center-left;");
            
            // Quantity field
            Label qtyLabel = new Label("Qty:");
            qtyLabel.setStyle("-fx-font-size: 12px;");
            TextField quantityField = new TextField("1");
            quantityField.setPrefWidth(60);
            quantityField.setStyle("-fx-font-size: 12px;");
            
            // Expiration date picker
            Label expLabel = new Label("Expires:");
            expLabel.setStyle("-fx-font-size: 12px;");
            DatePicker datePicker = new DatePicker();
            datePicker.setValue(LocalDate.now().plusDays(getDefaultExpirationDays(item)));
            datePicker.setPrefWidth(140);
            datePicker.setStyle("-fx-font-size: 12px;");
            
            bottomRow.getChildren().addAll(qtyLabel, quantityField, expLabel, datePicker);
            
            itemBox.getChildren().addAll(topRow, bottomRow);
            ingredientsBox.getChildren().add(itemBox);
            
            ingredientCheckBoxes.add(checkBox);
            ingredientDatePickers.put(checkBox, datePicker);
            ingredientQuantityFields.put(checkBox, quantityField);
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
    
    private int getDefaultExpirationDays(String ingredient) {
        // Return default expiration days based on ingredient type
        String lowerItem = ingredient.toLowerCase();
        
        if (lowerItem.contains("milk") || lowerItem.contains("yogurt") || lowerItem.contains("cream")) {
            return 7; // Dairy products
        } else if (lowerItem.contains("meat") || lowerItem.contains("chicken") || lowerItem.contains("beef") || 
                   lowerItem.contains("pork") || lowerItem.contains("fish") || lowerItem.contains("seafood")) {
            return 3; // Fresh meat
        } else if (lowerItem.contains("bread") || lowerItem.contains("bagel") || lowerItem.contains("roll")) {
            return 5; // Bread products
        } else if (lowerItem.contains("lettuce") || lowerItem.contains("spinach") || lowerItem.contains("kale") || 
                   lowerItem.contains("herbs") || lowerItem.contains("cilantro") || lowerItem.contains("parsley")) {
            return 4; // Leafy greens
        } else if (lowerItem.contains("banana") || lowerItem.contains("avocado") || lowerItem.contains("tomato")) {
            return 5; // Quick-ripening fruits
        } else if (lowerItem.contains("apple") || lowerItem.contains("orange") || lowerItem.contains("citrus")) {
            return 14; // Longer-lasting fruits
        } else if (lowerItem.contains("potato") || lowerItem.contains("onion") || lowerItem.contains("garlic") || 
                   lowerItem.contains("carrot") || lowerItem.contains("cabbage")) {
            return 21; // Root vegetables and hardy vegetables
        } else if (lowerItem.contains("egg")) {
            return 21; // Eggs
        } else if (lowerItem.contains("cheese")) {
            return 14; // Cheese
        } else {
            return 7; // Default for other items
        }
    }

    @FXML
    private void addSelectedToInventory() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "No user logged in!");
            return;
        }

        int successCount = 0;
        for (CheckBox checkBox : ingredientCheckBoxes) {
            if (checkBox.isSelected()) {
                // Get the ingredient name from the label
                VBox itemBox = (VBox) checkBox.getParent().getParent(); // checkbox -> topRow -> itemBox
                HBox topRow = (HBox) checkBox.getParent();
                Label label = (Label) topRow.getChildren().get(1); // Second child is the label
                String ingredientText = label.getText();
                String ingredientName = ingredientText.substring(ingredientText.indexOf(" ") + 1).trim();
                
                // Get quantity and expiration date
                DatePicker datePicker = ingredientDatePickers.get(checkBox);
                TextField quantityField = ingredientQuantityFields.get(checkBox);
                
                try {
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    LocalDate expirationDate = datePicker.getValue();
                    
                    if (expirationDate == null) {
                        expirationDate = LocalDate.now().plusDays(7); // Default if no date selected
                    }
                    
                    if (DatabaseHelper.addToInventory(currentUser, ingredientName, quantity, expirationDate)) {
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid quantity for " + ingredientName + ": " + quantityField.getText());
                    // Still try to add with quantity 1
                    LocalDate expirationDate = datePicker.getValue();
                    if (expirationDate == null) {
                        expirationDate = LocalDate.now().plusDays(7);
                    }
                    if (DatabaseHelper.addToInventory(currentUser, ingredientName, 1, expirationDate)) {
                        successCount++;
                    }
                }
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

    private String getIngredientEmoji(String ingredient) {
        String lowerIngredient = ingredient.toLowerCase();
        if (lowerIngredient.contains("carrot")) return "🥕";
        if (lowerIngredient.contains("tomato")) return "🍅";
        if (lowerIngredient.contains("spinach")) return "🥬";
        if (lowerIngredient.contains("broccoli")) return "🥦";
        if (lowerIngredient.contains("onion")) return "🧅";
        if (lowerIngredient.contains("garlic")) return "🧄";
        if (lowerIngredient.contains("pepper")) return "🌶️";
        if (lowerIngredient.contains("potato")) return "🥔";
        if (lowerIngredient.contains("mushroom")) return "🍄";
        if (lowerIngredient.contains("banana")) return "🍌";
        if (lowerIngredient.contains("apple")) return "🍎";
        if (lowerIngredient.contains("milk")) return "🥛";
        if (lowerIngredient.contains("egg")) return "🥚";
        if (lowerIngredient.contains("cheese")) return "🧀";
        if (lowerIngredient.contains("bread")) return "🍞";
        if (lowerIngredient.contains("chicken")) return "🍗";
        if (lowerIngredient.contains("rice")) return "🍚";
        if (lowerIngredient.contains("bean")) return "🥫";
        if (lowerIngredient.contains("zucchini")) return "🥒";
        if (lowerIngredient.contains("cabbage")) return "🥬";
        if (lowerIngredient.contains("kale")) return "🥬";
        if (lowerIngredient.contains("celery")) return "🥬";
        return "🥗"; // Default emoji for other ingredients
    }

    // 🔁 Navigation actions
    @FXML private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML private void goToInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML private void goToRecipes() throws IOException {
        App.setRoot("recipe");
    }

    @FXML private void goToStatistics() throws IOException {
        App.setRoot("statistics");
    }

    @FXML private void handleLogout() throws IOException {
        App.setRoot("start");
    }
}
