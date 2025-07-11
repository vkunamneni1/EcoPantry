package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;
import com.vedakunamneni.click.services.RecipeService;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class RecipeController implements Initializable {

    @FXML
    private VBox recipeBox;

    @FXML
    private VBox shoppingListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadRecipes();
        loadShoppingList();
    }

    private void loadRecipes() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showNoUserMessage();
            return;
        }

        // Show loading screen
        showLoadingScreen();

        // Create background task for recipe fetching
        Task<List<Recipe>> recipeTask = new Task<List<Recipe>>() {
            @Override
            protected List<Recipe> call() throws Exception {
                // Get user's inventory
                List<Ingredient> userIngredients = DatabaseHelper.getUserInventory(currentUser);
                // Get recipes based on user's ingredients (this might take time)
                return RecipeService.getRecipesForIngredients(userIngredients);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Recipe> recommendedRecipes = getValue();
                    // Get user's inventory again for display
                    List<Ingredient> userIngredients = DatabaseHelper.getUserInventory(currentUser);
                    displayRecipes(recommendedRecipes, userIngredients);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showRecipeErrorMessage();
                });
            }
        };
        
        // Run the task in background thread
        Thread recipeThread = new Thread(recipeTask);
        recipeThread.setDaemon(true);
        recipeThread.start();
    }
    
    private void showLoadingScreen() {
        recipeBox.getChildren().clear();
        
        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        loadingBox.getStyleClass().add("loading-container");
        
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(60, 60);
        
        Label loadingLabel = new Label("Loading delicious recipes...");
        loadingLabel.getStyleClass().add("loading-title");
        
        Label subLabel = new Label("Finding recipes based on your ingredients");
        subLabel.getStyleClass().add("loading-subtitle");
        
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel, subLabel);
        recipeBox.getChildren().add(loadingBox);
    }
    
    private void displayRecipes(List<Recipe> recommendedRecipes, List<Ingredient> userIngredients) {
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
            
            // Add Favorites Section
            loadFavoritesSection();
            
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
        // Show loading screen
        showLoadingScreen();
        
        // Create background task for fetching all recipes
        Task<List<Recipe>> allRecipesTask = new Task<List<Recipe>>() {
            @Override
            protected List<Recipe> call() throws Exception {
                return RecipeService.getRandomRecipes(20);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<Recipe> allRecipes = getValue();
                    displayAllRecipes(allRecipes);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showRecipeErrorMessage();
                });
            }
        };
        
        // Run the task in background thread
        Thread allRecipesThread = new Thread(allRecipesTask);
        allRecipesThread.setDaemon(true);
        allRecipesThread.start();
    }
    
    private void displayAllRecipes(List<Recipe> allRecipes) {
        // Clear and show all recipes regardless of user ingredients
        recipeBox.getChildren().clear();
        
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
    
    private void showRecipeErrorMessage() {
        recipeBox.getChildren().clear();
        
        VBox errorBox = new VBox(15);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.getStyleClass().add("error-container");
        
        Label errorLabel = new Label("⚠️ Failed to Load Recipes");
        errorLabel.getStyleClass().add("error-title");
        
        Label messageLabel = new Label("Unable to connect to recipe service. Please check your internet connection and try again.");
        messageLabel.getStyleClass().add("error-message");
        messageLabel.setWrapText(true);
        
        Button retryButton = new Button("Retry");
        retryButton.getStyleClass().add("retry-button");
        retryButton.setOnAction(e -> loadRecipes());
        
        errorBox.getChildren().addAll(errorLabel, messageLabel, retryButton);
        recipeBox.getChildren().add(errorBox);
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
    private void goToShoppingList() throws IOException {
        App.setRoot("shopping_list");
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

    private void loadFavoritesSection() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;
        
        List<String> favoriteRecipes = DatabaseHelper.getFavoriteRecipes(currentUser);
        
        if (!favoriteRecipes.isEmpty()) {
            // Add spacing
            VBox spacingBox = new VBox();
            spacingBox.setPrefHeight(30);
            recipeBox.getChildren().add(spacingBox);
            
            // Create favorites section
            VBox favoritesSection = new VBox(15);
            favoritesSection.getStyleClass().add("favorites-section");
            
            Label favoritesTitle = new Label("⭐ Your Favorite Recipes");
            favoritesTitle.getStyleClass().add("favorites-title");
            favoritesSection.getChildren().add(favoritesTitle);
            
            VBox favoritesContainer = new VBox(8);
            
            for (String recipeName : favoriteRecipes) {
                HBox favoriteItem = new HBox(10);
                favoriteItem.getStyleClass().add("favorite-recipe-item");
                favoriteItem.setStyle("-fx-alignment: center-left;");
                
                Label nameLabel = new Label(recipeName);
                nameLabel.getStyleClass().add("favorite-recipe-name");
                
                // Add spacer
                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                
                Button removeButton = new Button("✕");
                removeButton.getStyleClass().add("favorite-recipe-remove");
                removeButton.setOnAction(e -> {
                    DatabaseHelper.removeFromFavorites(currentUser, recipeName);
                    loadRecipes(); // Refresh the page
                    loadShoppingList(); // Refresh shopping list too
                });
                
                favoriteItem.getChildren().addAll(nameLabel, spacer, removeButton);
                favoritesContainer.getChildren().add(favoriteItem);
            }
            
            favoritesSection.getChildren().add(favoritesContainer);
            recipeBox.getChildren().add(favoritesSection);
        }
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
}
