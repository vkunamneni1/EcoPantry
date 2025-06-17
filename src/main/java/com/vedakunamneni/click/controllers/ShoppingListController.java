package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class ShoppingListController implements Initializable {

    @FXML
    private VBox shoppingListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadShoppingList();
    }

    private void loadShoppingList() {
        if (shoppingListContainer == null) return;
        
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) return;
        
        shoppingListContainer.getChildren().clear();
        
        List<ShoppingListItem> shoppingList = DatabaseHelper.getShoppingListWithIds(userEmail);
        
        if (shoppingList.isEmpty()) {
            Label emptyLabel = new Label("Your shopping list is empty");
            emptyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-font-size: 16px; -fx-padding: 20;");
            shoppingListContainer.getChildren().add(emptyLabel);
        } else {
            for (ShoppingListItem item : shoppingList) {
                VBox itemBox = createShoppingListItemBox(item);
                shoppingListContainer.getChildren().add(itemBox);
            }
        }
    }

    private VBox createShoppingListItemBox(ShoppingListItem item) {
        VBox itemBox = new VBox(8);
        itemBox.getStyleClass().add("shopping-list-item");
        itemBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 12;");
        
        // Create main content row
        HBox mainRow = new HBox(10);
        mainRow.setStyle("-fx-alignment: center-left;");
        
        // Item label
        Label itemLabel = new Label(item.ingredientName + " (for " + item.recipeName + ")");
        itemLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 500;");
        
        // Spacer to push dropdown to the right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Create action dropdown
        ComboBox<String> actionDropdown = new ComboBox<>();
        actionDropdown.getItems().addAll("Select Action", "Remove", "Add to Inventory");
        actionDropdown.setValue("Select Action");
        actionDropdown.setStyle("-fx-font-size: 12px; -fx-padding: 5 10;");
        
        // Handle dropdown selection
        actionDropdown.setOnAction(e -> {
            String selectedAction = actionDropdown.getValue();
            if ("Remove".equals(selectedAction)) {
                removeShoppingListItem(item);
            } else if ("Add to Inventory".equals(selectedAction)) {
                addToInventoryFromShoppingList(item);
            }
            // Reset dropdown
            actionDropdown.setValue("Select Action");
        });
        
        mainRow.getChildren().addAll(itemLabel, spacer, actionDropdown);
        itemBox.getChildren().add(mainRow);
        
        return itemBox;
    }

    private void removeShoppingListItem(ShoppingListItem item) {
        if (DatabaseHelper.removeFromShoppingList(item.id)) {
            loadShoppingList(); // Refresh the list
        }
    }

    private void addToInventoryFromShoppingList(ShoppingListItem item) {
        // Show dialog to get quantity and expiration date
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add to Inventory");
        dialog.setHeaderText("Add " + item.ingredientName + " to your inventory");
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setText("1");
        
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(7)); // Default 7 days from now
        
        grid.add(new Label("Quantity:"), 0, 0);
        grid.add(quantityField, 1, 0);
        grid.add(new Label("Expiration Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Add buttons
        javafx.scene.control.ButtonType addButtonType = new javafx.scene.control.ButtonType("Add", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        // Handle result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    LocalDate expirationDate = datePicker.getValue();
                    
                    if (quantity > 0 && expirationDate != null) {
                        String userEmail = SessionManager.getCurrentUser();
                        if (userEmail != null) {
                            // Add to inventory
                            boolean added = DatabaseHelper.addToInventory(userEmail, item.ingredientName, quantity, expirationDate);
                            if (added) {
                                // Remove from shopping list
                                DatabaseHelper.removeFromShoppingList(item.id);
                                return true;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid quantity
                }
            }
            return false;
        });
        
        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadShoppingList(); // Refresh the list
                showAlert("Success", item.ingredientName + " has been added to your inventory and removed from shopping list!");
            } else {
                showAlert("Error", "Failed to add item to inventory. Please check your input.");
            }
        });
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void clearAllShoppingList() {
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) return;
        
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear Shopping List");
        confirmAlert.setHeaderText("Remove all items from shopping list?");
        confirmAlert.setContentText("Are you sure you want to clear your entire shopping list?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (DatabaseHelper.clearShoppingList(userEmail)) {
                    loadShoppingList(); // Refresh the list
                }
            }
        });
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
    private void handleLogout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
    }

    // Inner class to represent shopping list items with IDs
    public static class ShoppingListItem {
        public final int id;
        public final String ingredientName;
        public final String recipeName;
        
        public ShoppingListItem(int id, String ingredientName, String recipeName) {
            this.id = id;
            this.ingredientName = ingredientName;
            this.recipeName = recipeName;
        }
    }
}
