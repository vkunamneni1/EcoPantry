package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Recipe;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class FavoritesController implements Initializable {

    @FXML private VBox favoritesContainer;
    @FXML private VBox emptyStateContainer;
    @FXML private Button clearFavoritesButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFavorites();
    }

    private void loadFavorites() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        List<String> favoriteRecipeNames = DatabaseHelper.getFavoriteRecipes(currentUser);
        
        favoritesContainer.getChildren().clear();
        
        if (favoriteRecipeNames.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            
            // Add header
            Label headerLabel = new Label("⭐ Your Favorite Recipes (" + favoriteRecipeNames.size() + ")");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-padding: 0 0 20 0;");
            favoritesContainer.getChildren().add(headerLabel);
            
            // Create tile pane for recipe cards
            TilePane tilePane = new TilePane();
            tilePane.setHgap(20);
            tilePane.setVgap(20);
            tilePane.setPrefColumns(3);
            
            // Add favorite recipe cards
            for (String recipeName : favoriteRecipeNames) {
                VBox recipeCard = createFavoriteRecipeCard(recipeName);
                tilePane.getChildren().add(recipeCard);
            }
            
            favoritesContainer.getChildren().add(tilePane);
            
            // Update button states
            clearFavoritesButton.setDisable(false);
        }
    }

    private VBox createFavoriteRecipeCard(String recipeName) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; " +
                     "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2); " +
                     "-fx-cursor: hand;");
        
        // Recipe name
        Label nameLabel = new Label(recipeName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        nameLabel.setWrapText(true);
        nameLabel.setPrefWidth(220);
        
        // Favorite indicator
        Label favoriteLabel = new Label("⭐ Favorited");
        favoriteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #E91E63; -fx-font-weight: bold;");
        
        // Action buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setStyle("-fx-alignment: center;");
        
        Button viewButton = new Button("👁️ View");
        viewButton.setStyle("-fx-font-size: 11px; -fx-background-color: #2196F3; -fx-text-fill: white; " +
                           "-fx-padding: 5 10; -fx-background-radius: 15; -fx-cursor: hand;");
        viewButton.setOnAction(e -> viewFavoriteRecipe(recipeName));
        
        Button removeButton = new Button("🗑️");
        removeButton.setStyle("-fx-font-size: 11px; -fx-background-color: #f44336; -fx-text-fill: white; " +
                             "-fx-padding: 5 8; -fx-background-radius: 15; -fx-cursor: hand;");
        removeButton.setOnAction(e -> removeFavoriteRecipe(recipeName));
        
        buttonBox.getChildren().addAll(viewButton, removeButton);
        
        // Add spacer to push buttons to bottom
        VBox spacer = new VBox();
        spacer.setStyle("-fx-pref-height: 10;");
        
        card.getChildren().addAll(nameLabel, favoriteLabel, spacer, buttonBox);
        
        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-background-color: #f5f5f5;", "-fx-background-color: white;")));
        
        return card;
    }

    private void viewFavoriteRecipe(String recipeName) {
        // Try to find the recipe in the recipe service first
        try {
            // For now, we'll navigate to the recipes page and let the user find it
            // In a more advanced implementation, we could store complete recipe data
            // and recreate the Recipe object from the stored data
            showAlert("Info", "Please find \"" + recipeName + "\" in the Recipes section to view details.");
            App.setRoot("recipe");
        } catch (IOException e) {
            showAlert("Error", "Failed to navigate to recipes page.");
        }
    }

    private void removeFavoriteRecipe(String recipeName) {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            if (DatabaseHelper.removeFromFavorites(currentUser, recipeName)) {
                loadFavorites(); // Refresh the list
                showAlert("Success", "Recipe removed from favorites!");
            } else {
                showAlert("Error", "Failed to remove recipe from favorites.");
            }
        }
    }

    private void showEmptyState() {
        favoritesContainer.setVisible(false);
        emptyStateContainer.setVisible(true);
        clearFavoritesButton.setDisable(true);
    }

    private void hideEmptyState() {
        favoritesContainer.setVisible(true);
        emptyStateContainer.setVisible(false);
    }

    @FXML
    private void clearFavorites() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            List<String> favoriteRecipes = DatabaseHelper.getFavoriteRecipes(currentUser);
            
            // Remove all favorites
            boolean success = true;
            for (String recipeName : favoriteRecipes) {
                if (!DatabaseHelper.removeFromFavorites(currentUser, recipeName)) {
                    success = false;
                }
            }
            
            if (success) {
                loadFavorites();
                showAlert("Success", "All favorites cleared!");
            } else {
                showAlert("Warning", "Some favorites could not be removed.");
                loadFavorites(); // Refresh anyway
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
