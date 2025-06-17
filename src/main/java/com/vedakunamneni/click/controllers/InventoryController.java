package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class InventoryController implements Initializable {

    @FXML
    private VBox inventoryContainer;

    @FXML
    private VBox shoppingListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserInventory();
        loadShoppingList();
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
            // Group ingredients by name
            java.util.Map<String, java.util.List<Ingredient>> groupedIngredients = new java.util.HashMap<>();
            for (Ingredient ingredient : inventory) {
                groupedIngredients.computeIfAbsent(ingredient.getName(), k -> new java.util.ArrayList<>()).add(ingredient);
            }
            
            for (java.util.Map.Entry<String, java.util.List<Ingredient>> entry : groupedIngredients.entrySet()) {
                String ingredientName = entry.getKey();
                java.util.List<Ingredient> ingredients = entry.getValue();
                
                // Calculate total quantity
                int totalQuantity = ingredients.stream().mapToInt(Ingredient::getQuantity).sum();
                
                // Create main grouped item box
                VBox mainItemBox = new VBox(5);
                mainItemBox.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
                mainItemBox.setPadding(new Insets(10));
                
                // Create clickable header
                HBox headerBox = new HBox(10);
                headerBox.setStyle("-fx-alignment: center-left; -fx-cursor: hand;");
                
                // Add emoji based on item name
                String emoji = getIngredientEmoji(ingredientName);
                Label itemLabel = new Label(emoji + " " + ingredientName);
                itemLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                
                // Add total quantity label
                Label quantityLabel = new Label("Total Qty: " + totalQuantity + " (" + ingredients.size() + " batches)");
                quantityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                
                // Add spacer to push buttons to the right
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                // Add "Clear All" button for this item type
                Button clearAllButton = new Button("Clear All");
                clearAllButton.setStyle("-fx-font-size: 10px; -fx-background-color: #f59e0b; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 5 10;");
                clearAllButton.setOnAction(e -> {
                    e.consume(); // Prevent header click event
                    handleClearAllItems(ingredientName, ingredients);
                });
                
                // Add expand/collapse indicator
                Label expandLabel = new Label("▼ Click to expand");
                expandLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
                
                headerBox.getChildren().addAll(itemLabel, quantityLabel, spacer, clearAllButton, expandLabel);
                
                // Create details container (initially hidden)
                VBox detailsContainer = new VBox(5);
                detailsContainer.setVisible(false);
                detailsContainer.setManaged(false);
                detailsContainer.setStyle("-fx-padding: 10 0 0 20; -fx-border-color: #ddd; -fx-border-width: 1 0 0 2; -fx-border-style: solid;");
                
                // Add individual items to details container
                for (Ingredient ingredient : ingredients) {
                    HBox itemDetailBox = new HBox(10);
                    itemDetailBox.setStyle("-fx-alignment: center-left; -fx-padding: 5;");
                    
                    Label detailLabel = new Label("Batch ID: " + ingredient.getId());
                    detailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                    
                    Label qtyLabel = new Label("Qty: " + ingredient.getQuantity());
                    qtyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                    
                    Label expirationLabel = createExpirationLabel(ingredient);
                    expirationLabel.setStyle("-fx-font-size: 10px;");
                    
                    Button removeButton = new Button("Remove");
                    removeButton.setStyle("-fx-font-size: 9px; -fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-padding: 3 8;");
                    removeButton.setOnAction(e -> {
                        handleRemoveItem(ingredient);
                    });
                    
                    itemDetailBox.getChildren().addAll(detailLabel, qtyLabel, expirationLabel, removeButton);
                    detailsContainer.getChildren().add(itemDetailBox);
                }
                
                // Add click handler to toggle details
                headerBox.setOnMouseClicked(e -> {
                    boolean isVisible = detailsContainer.isVisible();
                    detailsContainer.setVisible(!isVisible);
                    detailsContainer.setManaged(!isVisible);
                    expandLabel.setText(isVisible ? "▼ Click to expand" : "▲ Click to collapse");
                });
                
                mainItemBox.getChildren().addAll(headerBox, detailsContainer);
                inventoryContainer.getChildren().add(mainItemBox);
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
    private void goToShoppingList() throws IOException {
        App.setRoot("shopping_list");
    }

    @FXML
    private void goToStatistics() throws IOException {
        App.setRoot("statistics");
        // The statistics page will auto-refresh when loaded
    }
    
    private void loadShoppingList() {
        if (shoppingListContainer == null) return;
        
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) return;
        
        shoppingListContainer.getChildren().clear();
        
        java.util.List<String> shoppingList = DatabaseHelper.getShoppingList(userEmail);
        
        if (shoppingList.isEmpty()) {
            javafx.scene.control.Label emptyLabel = new javafx.scene.control.Label("Your shopping list is empty");
            emptyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-font-size: 12px;");
            shoppingListContainer.getChildren().add(emptyLabel);
        } else {
            for (String item : shoppingList) {
                javafx.scene.layout.VBox itemBox = new javafx.scene.layout.VBox(3);
                itemBox.getStyleClass().add("shopping-list-item");
                
                javafx.scene.control.Label itemLabel = new javafx.scene.control.Label(item);
                itemLabel.getStyleClass().add("shopping-list-item-text");
                
                itemBox.getChildren().add(itemLabel);
                shoppingListContainer.getChildren().add(itemBox);
            }
        }
    }

    @FXML
    private void clearShoppingList() {
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) return;
        
        // Clear shopping list from database
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:ecopantry.db");
             java.sql.PreparedStatement pstmt = conn.prepareStatement("DELETE FROM shopping_list WHERE user_email = ?")) {
            pstmt.setString(1, userEmail);
            pstmt.executeUpdate();
            
            // Refresh the display
            loadShoppingList();
        } catch (java.sql.SQLException e) {
            System.err.println("Error clearing shopping list: " + e.getMessage());
        }
    }
    
    private void handleClearAllItems(String itemName, List<Ingredient> ingredients) {
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear All Items");
        confirmAlert.setHeaderText("Remove all " + itemName + " items?");
        confirmAlert.setContentText("Are you sure you want to remove all " + ingredients.size() + " batches of " + itemName + "?");
        
        javafx.scene.control.ButtonType usedButton = new javafx.scene.control.ButtonType("Used (All)");
        javafx.scene.control.ButtonType wastedButton = new javafx.scene.control.ButtonType("Wasted (All)");
        javafx.scene.control.ButtonType cancelButton = javafx.scene.control.ButtonType.CANCEL;
        
        confirmAlert.getButtonTypes().setAll(usedButton, wastedButton, cancelButton);
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == usedButton) {
                // Remove all items as used
                System.out.println("InventoryController: Clearing all items as USED");
                for (Ingredient ingredient : ingredients) {
                    DatabaseHelper.removeFromInventoryWithTracking(ingredient.getId(), true);
                }
                loadUserInventory(); // Refresh the list
            } else if (result.get() == wastedButton) {
                // Remove all items as wasted
                System.out.println("InventoryController: Clearing all items as WASTED");
                for (Ingredient ingredient : ingredients) {
                    DatabaseHelper.removeFromInventoryWithTracking(ingredient.getId(), false);
                }
                loadUserInventory(); // Refresh the list
            }
        }
    }
    
    private void handleRemoveItem(Ingredient ingredient) {
        // First, ask for quantity if the item has more than 1
        if (ingredient.getQuantity() > 1) {
            // Show quantity selection dialog
            javafx.scene.control.Dialog<Integer> quantityDialog = new javafx.scene.control.Dialog<>();
            quantityDialog.setTitle("Remove Items");
            quantityDialog.setHeaderText("How many " + ingredient.getName() + " do you want to remove?");
            
            // Create the content
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            javafx.scene.control.TextField quantityField = new javafx.scene.control.TextField();
            quantityField.setPromptText("Enter quantity (1-" + ingredient.getQuantity() + ")");
            quantityField.setText("1");
            
            javafx.scene.control.Button allButton = new javafx.scene.control.Button("Remove All (" + ingredient.getQuantity() + ")");
            allButton.setOnAction(e -> {
                quantityField.setText(String.valueOf(ingredient.getQuantity()));
            });
            
            grid.add(new javafx.scene.control.Label("Quantity:"), 0, 0);
            grid.add(quantityField, 1, 0);
            grid.add(allButton, 2, 0);
            
            quantityDialog.getDialogPane().setContent(grid);
            
            javafx.scene.control.ButtonType confirmButtonType = new javafx.scene.control.ButtonType("Continue", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            quantityDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, javafx.scene.control.ButtonType.CANCEL);
            
            quantityDialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    try {
                        int qty = Integer.parseInt(quantityField.getText());
                        if (qty > 0 && qty <= ingredient.getQuantity()) {
                            return qty;
                        }
                    } catch (NumberFormatException e) {
                        // Invalid input
                    }
                }
                return null;
            });
            
            java.util.Optional<Integer> quantityResult = quantityDialog.showAndWait();
            if (quantityResult.isPresent()) {
                int quantityToRemove = quantityResult.get();
                handleRemovalChoice(ingredient, quantityToRemove);
            }
        } else {
            // Only 1 item, proceed directly to usage choice
            handleRemovalChoice(ingredient, 1);
        }
    }
    
    private void handleRemovalChoice(Ingredient ingredient, int quantityToRemove) {
        // Show dialog to ask if item was used or wasted
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Item");
        alert.setHeaderText("How was this item handled?");
        
        String itemText = quantityToRemove == ingredient.getQuantity() ? 
            "all " + ingredient.getName() + " (Qty: " + ingredient.getQuantity() + ")" :
            quantityToRemove + " of " + ingredient.getName() + " (from " + ingredient.getQuantity() + ")";
            
        alert.setContentText("Was " + itemText + " used (consumed) or wasted (thrown away)?");
        
        javafx.scene.control.ButtonType usedButton = new javafx.scene.control.ButtonType("Used");
        javafx.scene.control.ButtonType wastedButton = new javafx.scene.control.ButtonType("Wasted");
        javafx.scene.control.ButtonType cancelButton = javafx.scene.control.ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(usedButton, wastedButton, cancelButton);
        
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            boolean wasUsed = result.get() == usedButton;
            if (result.get() == usedButton || result.get() == wastedButton) {
                if (quantityToRemove == ingredient.getQuantity()) {
                    // Remove entire item
                    System.out.println("InventoryController: Removing entire item with tracking - wasUsed: " + wasUsed);
                    DatabaseHelper.removeFromInventoryWithTracking(ingredient.getId(), wasUsed);
                } else {
                    // Reduce quantity
                    System.out.println("InventoryController: Reducing quantity and tracking - wasUsed: " + wasUsed);
                    int newQuantity = ingredient.getQuantity() - quantityToRemove;
                    DatabaseHelper.updateInventoryQuantity(ingredient.getId(), newQuantity);
                    
                    // Track the removed quantity as a statistic
                    String userEmail = SessionManager.getCurrentUser();
                    if (userEmail != null) {
                        String status = wasUsed ? "USED" : "WASTED";
                        int daysUntilExpiration = (int) ingredient.getDaysUntilExpiration();
                        System.out.println("InventoryController: About to track statistic - " + status);
                        DatabaseHelper.trackFoodStatistic(userEmail, ingredient.getName(), quantityToRemove, status, daysUntilExpiration);
                    }
                }
                loadUserInventory(); // Refresh the list
            }
        }
    }

    @FXML
    private void handleClearAllInventory() {
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) {
            return;
        }
        
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear All Inventory");
        confirmAlert.setHeaderText("Are you sure you want to clear your entire inventory?");
        confirmAlert.setContentText("This will permanently delete ALL items from your pantry. This action cannot be undone.");
        
        // Add custom buttons
        javafx.scene.control.ButtonType confirmButton = new javafx.scene.control.ButtonType("Yes, Clear All");
        javafx.scene.control.ButtonType cancelButton = javafx.scene.control.ButtonType.CANCEL;
        
        confirmAlert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            boolean success = DatabaseHelper.clearAllInventory(userEmail);
            if (success) {
                // Show success message
                javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                successAlert.setTitle("Inventory Cleared");
                successAlert.setHeaderText("Success!");
                successAlert.setContentText("Your entire inventory has been cleared successfully. You earned points for this action!");
                successAlert.showAndWait();
                
                // Refresh the inventory display
                loadUserInventory();
            } else {
                // Show error message
                javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to clear inventory");
                errorAlert.setContentText("An error occurred while clearing your inventory. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }
}
