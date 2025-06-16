package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;

public class RecipeDetailController implements Initializable {

    @FXML private Label recipeNameLabel;
    @FXML private Label recipeDescriptionLabel;
    @FXML private Label cookTimeLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label servingsLabel;
    @FXML private Label categoryLabel;
    @FXML private VBox colorIndicator;
    @FXML private VBox ingredientsContainer;
    @FXML private VBox instructionsContainer;
    @FXML private javafx.scene.control.Button favoriteButton;

    private Recipe currentRecipe;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentRecipe = SessionManager.getSelectedRecipe();
        if (currentRecipe != null) {
            displayRecipe(currentRecipe);
            updateFavoriteButton();
        } else {
            // Handle case where no recipe is selected
            recipeNameLabel.setText("No Recipe Selected");
            recipeDescriptionLabel.setText("Please select a recipe from the recipes page.");
        }
    }

    private void displayRecipe(Recipe recipe) {
        // Set basic info
        recipeNameLabel.setText(recipe.getName());
        recipeDescriptionLabel.setText(recipe.getDescription());
        cookTimeLabel.setText(recipe.getCookingTimeFormatted());
        difficultyLabel.setText(recipe.getDifficultyStars());
        servingsLabel.setText(String.valueOf(recipe.getServings()));
        categoryLabel.setText(recipe.getCategory());
        categoryLabel.setStyle(categoryLabel.getStyle() + "; -fx-background-color: " + recipe.getColor() + ";");
        
        // Set color indicator
        colorIndicator.setStyle("-fx-background-color: " + recipe.getColor() + "; -fx-background-radius: 4;");
        
        // Display ingredients
        displayIngredients(recipe);
        
        // Display instructions
        displayInstructions(recipe);
    }

    private void displayIngredients(Recipe recipe) {
        ingredientsContainer.getChildren().clear();
        
        // Get user's inventory to check what they have
        String currentUser = SessionManager.getCurrentUser();
        List<Ingredient> userIngredients = (currentUser != null) ? 
            DatabaseHelper.getUserInventory(currentUser) : 
            new java.util.ArrayList<>();
        
        for (String ingredient : recipe.getIngredients()) {
            HBox ingredientBox = new HBox(10);
            ingredientBox.setStyle("-fx-alignment: center-left; -fx-padding: 5 0;");
            
            // Check if user has this ingredient
            boolean hasIngredient = userIngredients.stream()
                .anyMatch(userIngredient -> 
                    userIngredient.getName().toLowerCase().contains(ingredient.toLowerCase()) ||
                    ingredient.toLowerCase().contains(userIngredient.getName().toLowerCase()));
            
            // Status indicator
            Label statusLabel = new Label(hasIngredient ? "✓" : "○");
            statusLabel.setTextFill(hasIngredient ? Color.GREEN : Color.GRAY);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            // Ingredient name
            Label ingredientLabel = new Label(ingredient);
            ingredientLabel.setStyle("-fx-font-size: 14px; " + 
                (hasIngredient ? "-fx-text-fill: #333;" : "-fx-text-fill: #666;"));
            
            // Availability text
            Label availabilityLabel = new Label(hasIngredient ? "(in pantry)" : "(need to buy)");
            availabilityLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + 
                (hasIngredient ? "green;" : "#ff6b6b;"));
            
            ingredientBox.getChildren().addAll(statusLabel, ingredientLabel, availabilityLabel);
            ingredientsContainer.getChildren().add(ingredientBox);
        }
        
        // Add summary
        int totalIngredients = recipe.getIngredients().size();
        int availableIngredients = (int) recipe.getIngredients().stream()
            .filter(ingredient -> userIngredients.stream()
                .anyMatch(userIngredient -> 
                    userIngredient.getName().toLowerCase().contains(ingredient.toLowerCase()) ||
                    ingredient.toLowerCase().contains(userIngredient.getName().toLowerCase())))
            .count();
        
        Label summaryLabel = new Label(availableIngredients + "/" + totalIngredients + " ingredients available");
        summaryLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        if (availableIngredients == totalIngredients) {
            summaryLabel.setTextFill(Color.GREEN);
        } else if (availableIngredients >= totalIngredients * 0.7) {
            summaryLabel.setTextFill(Color.ORANGE);
        } else {
            summaryLabel.setTextFill(Color.RED);
        }
        ingredientsContainer.getChildren().add(summaryLabel);
    }

    private void displayInstructions(Recipe recipe) {
        instructionsContainer.getChildren().clear();
        
        List<String> instructions = recipe.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            HBox stepBox = new HBox(15);
            stepBox.setStyle("-fx-alignment: top-left; -fx-padding: 8 0;");
            
            // Step number
            Label stepNumberLabel = new Label(String.valueOf(i + 1));
            stepNumberLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: " + currentRecipe.getColor() + "; " +
                "-fx-background-radius: 50%; -fx-min-width: 24; -fx-min-height: 24; " +
                "-fx-alignment: center; -fx-padding: 0;");
            stepNumberLabel.setPrefWidth(24);
            stepNumberLabel.setPrefHeight(24);
            
            // Instruction text
            Label instructionLabel = new Label(instructions.get(i));
            instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-wrap-text: true;");
            instructionLabel.setPrefWidth(450);
            
            stepBox.getChildren().addAll(stepNumberLabel, instructionLabel);
            instructionsContainer.getChildren().add(stepBox);
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("recipe");
    }

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
    private void handleLogout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
    }

    // Recipe action button handlers
    @FXML
    private void addMissingIngredientsToShoppingList() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentRecipe == null) {
            showAlert("Error", "Please log in to use this feature.");
            return;
        }

        // Get user's current inventory
        List<Ingredient> userIngredients = DatabaseHelper.getUserInventory(currentUser);
        List<String> userIngredientNames = userIngredients.stream()
            .map(ingredient -> ingredient.getName().toLowerCase())
            .collect(Collectors.toList());

        // Find missing ingredients
        List<String> missingIngredients = currentRecipe.getIngredients().stream()
            .filter(recipeIngredient -> 
                userIngredientNames.stream()
                    .noneMatch(userIngredient -> 
                        userIngredient.contains(recipeIngredient.toLowerCase()) ||
                        recipeIngredient.toLowerCase().contains(userIngredient)
                    )
            )
            .collect(Collectors.toList());

        if (missingIngredients.isEmpty()) {
            showAlert("Info", "You already have all ingredients for this recipe!");
            return;
        }

        // Add missing ingredients to shopping list
        int addedCount = 0;
        for (String ingredient : missingIngredients) {
            if (DatabaseHelper.addToShoppingList(currentUser, ingredient, currentRecipe.getName())) {
                addedCount++;
            }
        }

        if (addedCount > 0) {
            // Award points for using the shopping list feature
            DatabaseHelper.addPoints(currentUser, addedCount * 3, "adding " + addedCount + " ingredients to shopping list for " + currentRecipe.getName());
            
            showAlert("Success", addedCount + " missing ingredients added to your shopping list!");
        } else {
            showAlert("Error", "Failed to add ingredients to shopping list.");
        }
    }

    @FXML
    private void favoriteRecipe() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentRecipe == null) {
            showAlert("Error", "Please log in to use this feature.");
            return;
        }

        // Check if recipe is already favorited
        boolean isAlreadyFavorited = DatabaseHelper.isRecipeFavorited(currentUser, currentRecipe.getName());
        
        if (isAlreadyFavorited) {
            // Remove from favorites
            if (DatabaseHelper.removeFromFavorites(currentUser, currentRecipe.getName())) {
                updateFavoriteButton();
                showAlert("Info", "Recipe removed from favorites.");
            } else {
                showAlert("Error", "Failed to remove recipe from favorites.");
            }
        } else {
            // Add to favorites - store recipe as JSON-like string
            String recipeData = createRecipeDataString(currentRecipe);
            if (DatabaseHelper.addToFavorites(currentUser, currentRecipe.getName(), recipeData)) {
                // Award points for favoriting a recipe
                DatabaseHelper.addPoints(currentUser, 5, "favoriting recipe: " + currentRecipe.getName());
                updateFavoriteButton();
                showAlert("Success", "Recipe added to favorites!");
            } else {
                showAlert("Error", "Failed to add recipe to favorites.");
            }
        }
    }

    @FXML
    private void copyRecipe() {
        if (currentRecipe == null) {
            showAlert("Error", "No recipe selected.");
            return;
        }

        // Create a formatted text version of the recipe
        StringBuilder recipeText = new StringBuilder();
        recipeText.append("🍽️ ").append(currentRecipe.getName()).append("\n\n");
        recipeText.append("📝 Description: ").append(currentRecipe.getDescription()).append("\n\n");
        recipeText.append("⏱️ Cook Time: ").append(currentRecipe.getCookingTimeFormatted()).append("\n");
        recipeText.append("🔥 Difficulty: ").append(currentRecipe.getDifficultyStars()).append(" (").append(currentRecipe.getDifficultyText()).append(")\n");
        recipeText.append("🍽️ Servings: ").append(currentRecipe.getServings()).append("\n");
        recipeText.append("📂 Category: ").append(currentRecipe.getCategory()).append("\n\n");
        
        recipeText.append("🛒 INGREDIENTS:\n");
        for (String ingredient : currentRecipe.getIngredients()) {
            recipeText.append("• ").append(ingredient).append("\n");
        }
        
        recipeText.append("\n📋 INSTRUCTIONS:\n");
        for (int i = 0; i < currentRecipe.getInstructions().size(); i++) {
            recipeText.append(i + 1).append(". ").append(currentRecipe.getInstructions().get(i)).append("\n");
        }
        
        recipeText.append("\n---\nCopied from EcoPantry 🌱");

        // Copy to clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(recipeText.toString());
        clipboard.setContent(content);

        // Award points for copying recipe
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            DatabaseHelper.addPoints(currentUser, 2, "copying recipe: " + currentRecipe.getName());
        }

        showAlert("Success", "Recipe copied to clipboard!");
    }

    // Helper methods
    private void updateFavoriteButton() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentRecipe != null && favoriteButton != null) {
            boolean isFavorited = DatabaseHelper.isRecipeFavorited(currentUser, currentRecipe.getName());
            if (isFavorited) {
                favoriteButton.setText("💔 Remove from Favorites");
                favoriteButton.setStyle("-fx-font-size: 14px; -fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 12 20; -fx-background-radius: 5;");
            } else {
                favoriteButton.setText("⭐ Favorite Recipe");
                favoriteButton.setStyle("-fx-font-size: 14px; -fx-background-color: #E91E63; -fx-text-fill: white; -fx-padding: 12 20; -fx-background-radius: 5;");
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

    private String createRecipeDataString(Recipe recipe) {
        // Create a simple string representation of the recipe data
        StringBuilder data = new StringBuilder();
        data.append("Name: ").append(recipe.getName()).append("|");
        data.append("Description: ").append(recipe.getDescription()).append("|");
        data.append("CookTime: ").append(recipe.getCookingTimeMinutes()).append("|");
        data.append("Difficulty: ").append(recipe.getDifficulty()).append("|");
        data.append("Category: ").append(recipe.getCategory()).append("|");
        data.append("Servings: ").append(recipe.getServings()).append("|");
        data.append("Ingredients: ").append(String.join(",", recipe.getIngredients())).append("|");
        data.append("Instructions: ").append(String.join(",", recipe.getInstructions()));
        return data.toString();
    }
}
