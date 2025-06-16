package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.util.List;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    @FXML
    private VBox inventoryContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserInventory();
    }

    private void loadUserInventory() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        List<String> inventory = DatabaseHelper.getUserInventory(currentUser);
        
        // Clear existing items
        inventoryContainer.getChildren().clear();
        
        if (inventory.isEmpty()) {
            Label emptyLabel = new Label("Your inventory is empty. Use the scanner to add ingredients!");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20; -fx-alignment: center;");
            inventoryContainer.getChildren().add(emptyLabel);
        } else {
            for (String item : inventory) {
                // Create an HBox for each inventory item
                javafx.scene.layout.HBox itemBox = new javafx.scene.layout.HBox(10);
                itemBox.getStyleClass().add("food-item");
                
                // Add emoji based on item name
                String emoji = getIngredientEmoji(item);
                Label itemLabel = new Label(emoji + " " + item);
                itemBox.getChildren().add(itemLabel);
                
                inventoryContainer.getChildren().add(itemBox);
            }
        }
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
        return "🥗"; // Default emoji for other ingredients
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.clearSession();
        App.setRoot("start");
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void goToScanner() throws IOException {
        App.setRoot("scanner");
    }
}
