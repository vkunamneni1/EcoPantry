package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.util.List;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;
import com.vedakunamneni.click.services.RecipeService;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;

public class RecipeController implements Initializable {

    @FXML
    private VBox recipeBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRecipes();
    }

    private void loadRecipes() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showNoUserMessage();
            return;
        }

        // Get user's inventory
        List<Ingredient> userIngredients = DatabaseHelper.getUserInventory(currentUser);
        
        // Get recipes based on user's ingredients
        List<Recipe> recommendedRecipes = RecipeService.getRecipesForIngredients(userIngredients);
        
        // Clear existing content
        recipeBox.getChildren().clear();
        
        if (recommendedRecipes.isEmpty()) {
            showNoRecipesMessage();
        } else {
            // Create title
            Label titleLabel = new Label("Recipes You Can Make");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            recipeBox.getChildren().add(titleLabel);
            
            // Create subtitle
            Label subtitleLabel = new Label("Based on ingredients in your pantry");
            subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 0 0 20 0;");
            recipeBox.getChildren().add(subtitleLabel);
            
            // Create tile pane for recipe cards
            TilePane tilePane = new TilePane();
            tilePane.setHgap(20);
            tilePane.setVgap(20);
            tilePane.setPrefColumns(3);
            
            for (Recipe recipe : recommendedRecipes) {
                VBox recipeCard = createRecipeCard(recipe, userIngredients);
                tilePane.getChildren().add(recipeCard);
            }
            
            // Wrap in scroll pane
            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            recipeBox.getChildren().add(scrollPane);
            
            // Add "Show All Recipes" button
            Button showAllButton = new Button("Browse All Recipes");
            showAllButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5;");
            showAllButton.setOnAction(e -> showAllRecipes());
            
            HBox buttonBox = new HBox();
            buttonBox.getChildren().add(showAllButton);
            buttonBox.setStyle("-fx-alignment: center; -fx-padding: 20 0 0 0;");
            
            recipeBox.getChildren().add(buttonBox);
        }
    }
    
    private VBox createRecipeCard(Recipe recipe, List<Ingredient> userIngredients) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setPrefHeight(320);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-background-color: white; " +
                     "-fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Recipe name
        Label nameLabel = new Label(recipe.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-wrap-text: true;");
        nameLabel.setMaxWidth(220);
        
        // Color indicator
        HBox colorBox = new HBox();
        colorBox.setPrefHeight(4);
        colorBox.setStyle("-fx-background-color: " + recipe.getColor() + "; -fx-background-radius: 2;");
        
        // Description
        Label descLabel = new Label(recipe.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");
        descLabel.setMaxWidth(220);
        descLabel.setPrefHeight(40);
        
        // Recipe info
        HBox infoBox = new HBox(15);
        infoBox.setStyle("-fx-alignment: center-left;");
        
        // Cooking time
        Label timeLabel = new Label("⏱️ " + recipe.getCookingTimeFormatted());
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        // Difficulty
        Label difficultyLabel = new Label("🔥 " + recipe.getDifficultyStars());
        difficultyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FF9800;");
        
        infoBox.getChildren().addAll(timeLabel, difficultyLabel);
        
        // Ingredient matching info
        int totalIngredients = recipe.getIngredients().size();
        int matchingIngredients = getMatchingIngredientsCount(recipe, userIngredients);
        
        Label matchLabel = new Label("✓ " + matchingIngredients + "/" + totalIngredients + " ingredients available");
        if (matchingIngredients == totalIngredients) {
            matchLabel.setTextFill(Color.GREEN);
        } else if (matchingIngredients >= totalIngredients * 0.7) {
            matchLabel.setTextFill(Color.ORANGE);
        } else {
            matchLabel.setTextFill(Color.RED);
        }
        matchLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        
        // Category badge
        Label categoryLabel = new Label(recipe.getCategory());
        categoryLabel.setStyle("-fx-font-size: 10px; -fx-background-color: " + recipe.getColor() + 
                             "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10;");
        
        // View recipe button
        Button viewButton = new Button("View Recipe");
        viewButton.setStyle("-fx-font-size: 12px; -fx-background-color: #2196F3; -fx-text-fill: white; " +
                          "-fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        viewButton.setOnAction(e -> showRecipeDetails(recipe));
        
        HBox bottomBox = new HBox();
        bottomBox.setStyle("-fx-alignment: center;");
        bottomBox.getChildren().add(viewButton);
        
        card.getChildren().addAll(nameLabel, colorBox, descLabel, infoBox, matchLabel, categoryLabel, bottomBox);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 4);");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-background-color: white; " +
                         "-fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        });
        
        return card;
    }
    
    private int getMatchingIngredientsCount(Recipe recipe, List<Ingredient> userIngredients) {
        int count = 0;
        for (String recipeIngredient : recipe.getIngredients()) {
            for (Ingredient userIngredient : userIngredients) {
                if (userIngredient.getName().toLowerCase().contains(recipeIngredient.toLowerCase()) ||
                    recipeIngredient.toLowerCase().contains(userIngredient.getName().toLowerCase())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    private void showRecipeDetails(Recipe recipe) {
        try {
            // Store the selected recipe in session for the detail view
            SessionManager.setSelectedRecipe(recipe);
            App.setRoot("recipe_detail");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAllRecipes() {
        // Clear and show all recipes regardless of user ingredients
        recipeBox.getChildren().clear();
        
        List<Recipe> allRecipes = RecipeService.getAllRecipes();
        
        // Create title
        Label titleLabel = new Label("All Recipes");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        recipeBox.getChildren().add(titleLabel);
        
        // Create back button
        Button backButton = new Button("← Back to Recommended");
        backButton.setStyle("-fx-font-size: 12px; -fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-cursor: hand;");
        backButton.setOnAction(e -> loadRecipes());
        recipeBox.getChildren().add(backButton);
        
        // Create tile pane
        TilePane tilePane = new TilePane();
        tilePane.setHgap(20);
        tilePane.setVgap(20);
        tilePane.setPrefColumns(3);
        
        String currentUser = SessionManager.getCurrentUser();
        List<Ingredient> userIngredients = (currentUser != null) ? 
            DatabaseHelper.getUserInventory(currentUser) : 
            new java.util.ArrayList<>();
        
        for (Recipe recipe : allRecipes) {
            VBox recipeCard = createRecipeCard(recipe, userIngredients);
            tilePane.getChildren().add(recipeCard);
        }
        
        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        recipeBox.getChildren().add(scrollPane);
    }
    
    private void showNoUserMessage() {
        Label noUserLabel = new Label("Please log in to see personalized recipes");
        noUserLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 50;");
        recipeBox.getChildren().add(noUserLabel);
    }
    
    private void showNoRecipesMessage() {
        VBox noRecipesBox = new VBox(20);
        noRecipesBox.setStyle("-fx-alignment: center; -fx-padding: 50;");
        
        Label noRecipesLabel = new Label("No recipes found for your current ingredients");
        noRecipesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
        
        Label suggestionLabel = new Label("Try adding more ingredients to your pantry using the scanner!");
        suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        
        Button browseAllButton = new Button("Browse All Recipes");
        browseAllButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5;");
        browseAllButton.setOnAction(e -> showAllRecipes());
        
        noRecipesBox.getChildren().addAll(noRecipesLabel, suggestionLabel, browseAllButton);
        recipeBox.getChildren().add(noRecipesBox);
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
    private void goToScanner() throws IOException {
        App.setRoot("scanner");
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
    }
}
