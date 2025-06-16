package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.util.List;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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

    private Recipe currentRecipe;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentRecipe = SessionManager.getSelectedRecipe();
        if (currentRecipe != null) {
            displayRecipe(currentRecipe);
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
}
