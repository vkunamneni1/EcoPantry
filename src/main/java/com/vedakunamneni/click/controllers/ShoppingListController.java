package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShoppingListController implements Initializable {

    @FXML private VBox shoppingListContainer;
    @FXML private VBox emptyStateContainer;
    @FXML private Button clearListButton;
    @FXML private Button markAllPurchasedButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadShoppingList();
    }

    private void loadShoppingList() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        List<String> shoppingItems = DatabaseHelper.getShoppingList(currentUser);
        
        shoppingListContainer.getChildren().clear();
        
        if (shoppingItems.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            
            // Add header
            Label headerLabel = new Label("📋 Items to Buy");
            headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-padding: 0 0 10 0;");
            shoppingListContainer.getChildren().add(headerLabel);
            
            // Add shopping items
            for (String item : shoppingItems) {
                VBox itemCard = createShoppingItemCard(item);
                shoppingListContainer.getChildren().add(itemCard);
            }
            
            // Update button states
            clearListButton.setDisable(false);
            markAllPurchasedButton.setDisable(false);
        }
    }

    private VBox createShoppingItemCard(String item) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
        
        HBox mainRow = new HBox(15);
        mainRow.setStyle("-fx-alignment: center-left;");
        
        // Checkbox for marking as purchased
        CheckBox purchasedBox = new CheckBox();
        purchasedBox.setStyle("-fx-text-fill: #4CAF50;");
        
        // Item text
        Label itemLabel = new Label(item);
        itemLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        itemLabel.setPrefWidth(400);
        itemLabel.setWrapText(true);
        
        // Remove button
        Button removeButton = new Button("🗑️");
        removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; " +
                             "-fx-padding: 5 8; -fx-background-radius: 15; -fx-cursor: hand;");
        removeButton.setOnAction(e -> removeShoppingItem(item));
        
        mainRow.getChildren().addAll(purchasedBox, itemLabel, removeButton);
        
        // Handle checkbox action
        purchasedBox.setOnAction(e -> {
            if (purchasedBox.isSelected()) {
                markItemAsPurchased(item);
                loadShoppingList(); // Refresh the list
            }
        });
        
        card.getChildren().add(mainRow);
        return card;
    }

    private void removeShoppingItem(String item) {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Extract ingredient name from the item string (format: "ingredient (for recipe)")
            String ingredientName = extractIngredientName(item);
            String recipeName = extractRecipeName(item);
            
            if (DatabaseHelper.removeFromShoppingList(currentUser, ingredientName, recipeName)) {
                loadShoppingList(); // Refresh the list
                showAlert("Success", "Item removed from shopping list!");
            } else {
                showAlert("Error", "Failed to remove item from shopping list.");
            }
        }
    }

    private void markItemAsPurchased(String item) {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            String ingredientName = extractIngredientName(item);
            String recipeName = extractRecipeName(item);
            
            if (DatabaseHelper.markShoppingItemAsPurchased(currentUser, ingredientName, recipeName)) {
                // Award points for purchasing items
                DatabaseHelper.addPoints(currentUser, 3, "purchasing ingredient: " + ingredientName);
                showAlert("Success", "Item marked as purchased!");
            } else {
                showAlert("Error", "Failed to mark item as purchased.");
            }
        }
    }

    private String extractIngredientName(String item) {
        // Extract ingredient name from "ingredient (for recipe)" format
        int index = item.indexOf(" (for ");
        return index > 0 ? item.substring(0, index) : item;
    }

    private String extractRecipeName(String item) {
        // Extract recipe name from "ingredient (for recipe)" format
        int startIndex = item.indexOf(" (for ");
        int endIndex = item.lastIndexOf(")");
        if (startIndex > 0 && endIndex > startIndex) {
            return item.substring(startIndex + 6, endIndex);
        }
        return "";
    }

    private void showEmptyState() {
        shoppingListContainer.setVisible(false);
        emptyStateContainer.setVisible(true);
        clearListButton.setDisable(true);
        markAllPurchasedButton.setDisable(true);
    }

    private void hideEmptyState() {
        shoppingListContainer.setVisible(true);
        emptyStateContainer.setVisible(false);
    }

    @FXML
    private void clearShoppingList() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            if (DatabaseHelper.clearShoppingList(currentUser)) {
                loadShoppingList();
                showAlert("Success", "Shopping list cleared!");
            } else {
                showAlert("Error", "Failed to clear shopping list.");
            }
        }
    }

    @FXML
    private void markAllPurchased() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            List<String> items = DatabaseHelper.getShoppingList(currentUser);
            int itemCount = items.size();
            
            if (DatabaseHelper.markAllShoppingItemsAsPurchased(currentUser)) {
                // Award points for purchasing all items
                DatabaseHelper.addPoints(currentUser, itemCount * 3, "purchasing all " + itemCount + " shopping list items");
                loadShoppingList();
                showAlert("Success", "All items marked as purchased!");
            } else {
                showAlert("Error", "Failed to mark all items as purchased.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods
    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void goToInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML
    private void goToRecipes() throws IOException {
        App.setRoot("recipe");
    }

    @FXML
    private void goToScanner() throws IOException {
        App.setRoot("scanner");
    }

    @FXML
    private void goToStatistics() throws IOException {
        App.setRoot("statistics");
    }

    @FXML
    private void goToShoppingList() throws IOException {
        App.setRoot("shopping_list");
    }

    @FXML
    private void goToFavorites() throws IOException {
        App.setRoot("favorites");
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
    }
}
