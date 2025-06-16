package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

        List<Ingredient> inventory = DatabaseHelper.getUserInventory(currentUser);
        
        // Clear existing items
        inventoryContainer.getChildren().clear();
        
        if (inventory.isEmpty()) {
            Label emptyLabel = new Label("Your inventory is empty. Use the scanner to add ingredients!");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20; -fx-alignment: center;");
            inventoryContainer.getChildren().add(emptyLabel);
        } else {
            for (Ingredient ingredient : inventory) {
                // Create an HBox for each inventory item
                HBox itemBox = new HBox(10);
                itemBox.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-alignment: center-left;");
                itemBox.setPadding(new Insets(10));
                
                // Add emoji based on item name
                String emoji = getIngredientEmoji(ingredient.getName());
                Label itemLabel = new Label(emoji + " " + ingredient.getName());
                itemLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                
                // Add quantity label
                Label quantityLabel = new Label("Qty: " + ingredient.getQuantity());
                quantityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                
                // Add expiration status
                Label expirationLabel = createExpirationLabel(ingredient);
                
                // Add remove button
                Button removeButton = new Button("Remove");
                removeButton.setStyle("-fx-font-size: 10px; -fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3;");
                removeButton.setOnAction(e -> {
                    DatabaseHelper.removeFromInventory(ingredient.getId());
                    loadUserInventory(); // Refresh the list
                });
                
                itemBox.getChildren().addAll(itemLabel, quantityLabel, expirationLabel, removeButton);
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

    private Label createExpirationLabel(Ingredient ingredient) {
        long daysUntilExpiration = ingredient.getDaysUntilExpiration();
        Label expirationLabel = new Label();
        
        String expirationText;
        
        if (daysUntilExpiration < 0) {
            // Expired
            expirationText = "● EXPIRED (" + Math.abs(daysUntilExpiration) + " days ago)";
            expirationLabel.setTextFill(Color.RED);
        } else if (daysUntilExpiration == 0) {
            // Expires today
            expirationText = "● EXPIRES TODAY";
            expirationLabel.setTextFill(Color.RED);
        } else if (daysUntilExpiration <= 2) {
            // Expires soon (1-2 days)
            expirationText = "● Expires in " + daysUntilExpiration + " day" + (daysUntilExpiration == 1 ? "" : "s");
            expirationLabel.setTextFill(Color.RED);
        } else if (daysUntilExpiration <= 7) {
            // Expires this week (3-7 days)
            expirationText = "● Expires in " + daysUntilExpiration + " days";
            expirationLabel.setTextFill(Color.ORANGE);
        } else {
            // Fresh (8+ days)
            expirationText = "● Fresh (" + daysUntilExpiration + " days left)";
            expirationLabel.setTextFill(Color.GREEN);
        }
        
        expirationLabel.setText(expirationText);
        expirationLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        
        return expirationLabel;
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

    @FXML
    private void goToRecipes() throws IOException {
        App.setRoot("recipe");
    }

    @FXML
    private void goToStatistics() throws IOException {
        App.setRoot("statistics");
    }
}
