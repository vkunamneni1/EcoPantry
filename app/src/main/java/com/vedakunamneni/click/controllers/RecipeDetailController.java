package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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
            
            // Ingredient name with emoji
            String emoji = getIngredientEmoji(ingredient);
            Label ingredientLabel = new Label(emoji + " " + ingredient);
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

    private String getIngredientEmoji(String ingredient) {
        String lowerIngredient = ingredient.toLowerCase();
        
        // Fruits
        if (lowerIngredient.contains("apple")) return "🍎";
        if (lowerIngredient.contains("banana")) return "🍌";
        if (lowerIngredient.contains("orange")) return "🍊";
        if (lowerIngredient.contains("lemon")) return "🍋";
        if (lowerIngredient.contains("lime")) return "🍋";
        if (lowerIngredient.contains("grape")) return "🍇";
        if (lowerIngredient.contains("strawberry")) return "🍓";
        if (lowerIngredient.contains("raspberry")) return "🫐";
        if (lowerIngredient.contains("blueberry")) return "🫐";
        if (lowerIngredient.contains("blackberry")) return "🫐";
        if (lowerIngredient.contains("cherry")) return "🍒";
        if (lowerIngredient.contains("peach")) return "🍑";
        if (lowerIngredient.contains("pear")) return "🍐";
        if (lowerIngredient.contains("pineapple")) return "🍍";
        if (lowerIngredient.contains("mango")) return "🥭";
        if (lowerIngredient.contains("avocado")) return "🥑";
        if (lowerIngredient.contains("coconut")) return "🥥";
        if (lowerIngredient.contains("kiwi")) return "🥝";
        if (lowerIngredient.contains("watermelon")) return "🍉";
        if (lowerIngredient.contains("melon")) return "🍈";
        if (lowerIngredient.contains("cantaloupe")) return "🍈";
        if (lowerIngredient.contains("papaya")) return "🥭";
        if (lowerIngredient.contains("plum")) return "🟣";
        if (lowerIngredient.contains("apricot")) return "🍑";
        
        // Vegetables
        if (lowerIngredient.contains("tomato")) return "🍅";
        if (lowerIngredient.contains("carrot")) return "🥕";
        if (lowerIngredient.contains("broccoli")) return "🥦";
        if (lowerIngredient.contains("spinach")) return "🥬";
        if (lowerIngredient.contains("lettuce")) return "🥬";
        if (lowerIngredient.contains("cabbage")) return "🥬";
        if (lowerIngredient.contains("kale")) return "🥬";
        if (lowerIngredient.contains("arugula")) return "🥬";
        if (lowerIngredient.contains("onion")) return "🧅";
        if (lowerIngredient.contains("garlic")) return "🧄";
        if (lowerIngredient.contains("ginger")) return "🫚";
        if (lowerIngredient.contains("potato")) return "🥔";
        if (lowerIngredient.contains("sweet potato")) return "🍠";
        if (lowerIngredient.contains("mushroom")) return "🍄";
        if (lowerIngredient.contains("bell pepper")) return "🫑";
        if (lowerIngredient.contains("pepper") && !lowerIngredient.contains("bell")) return "🌶️";
        if (lowerIngredient.contains("chili")) return "🌶️";
        if (lowerIngredient.contains("jalapeno")) return "🌶️";
        if (lowerIngredient.contains("cucumber")) return "🥒";
        if (lowerIngredient.contains("zucchini")) return "🥒";
        if (lowerIngredient.contains("eggplant")) return "🍆";
        if (lowerIngredient.contains("corn")) return "🌽";
        if (lowerIngredient.contains("peas")) return "🟢";
        if (lowerIngredient.contains("green bean")) return "🥦";
        if (lowerIngredient.contains("asparagus")) return "🥦";
        if (lowerIngredient.contains("celery")) return "🥬";
        if (lowerIngredient.contains("radish")) return "🟣";
        if (lowerIngredient.contains("beet")) return "🔴";
        if (lowerIngredient.contains("turnip")) return "⚪";
        if (lowerIngredient.contains("parsnip")) return "⚪";
        if (lowerIngredient.contains("leek")) return "🧅";
        if (lowerIngredient.contains("shallot")) return "🧅";
        if (lowerIngredient.contains("scallion")) return "🌿";
        if (lowerIngredient.contains("green onion")) return "🌿";
        if (lowerIngredient.contains("chive")) return "🌿";
        
        // Grains & Cereals
        if (lowerIngredient.contains("rice")) return "🍚";
        if (lowerIngredient.contains("bread")) return "🍞";
        if (lowerIngredient.contains("pasta")) return "🍝";
        if (lowerIngredient.contains("noodle")) return "🍜";
        if (lowerIngredient.contains("oat")) return "🥣";
        if (lowerIngredient.contains("quinoa")) return "🌾";
        if (lowerIngredient.contains("barley")) return "🌾";
        if (lowerIngredient.contains("wheat")) return "🌾";
        if (lowerIngredient.contains("flour")) return "🌾";
        if (lowerIngredient.contains("cereal")) return "🥣";
        if (lowerIngredient.contains("bagel")) return "🥯";
        if (lowerIngredient.contains("croissant")) return "🥐";
        if (lowerIngredient.contains("pretzel")) return "🥨";
        
        // Proteins
        if (lowerIngredient.contains("chicken")) return "🍗";
        if (lowerIngredient.contains("beef")) return "🥩";
        if (lowerIngredient.contains("pork")) return "🥩";
        if (lowerIngredient.contains("lamb")) return "🥩";
        if (lowerIngredient.contains("turkey")) return "🦃";
        if (lowerIngredient.contains("duck")) return "🦆";
        if (lowerIngredient.contains("fish")) return "🐟";
        if (lowerIngredient.contains("salmon")) return "🐟";
        if (lowerIngredient.contains("tuna")) return "🐟";
        if (lowerIngredient.contains("shrimp")) return "🦐";
        if (lowerIngredient.contains("crab")) return "🦀";
        if (lowerIngredient.contains("lobster")) return "🦞";
        if (lowerIngredient.contains("scallop")) return "🐚";
        if (lowerIngredient.contains("oyster")) return "🦪";
        if (lowerIngredient.contains("mussel")) return "🐚";
        if (lowerIngredient.contains("clam")) return "🐚";
        if (lowerIngredient.contains("squid")) return "🦑";
        if (lowerIngredient.contains("octopus")) return "🐙";
        if (lowerIngredient.contains("egg")) return "🥚";
        if (lowerIngredient.contains("tofu")) return "⬜";
        if (lowerIngredient.contains("tempeh")) return "🟤";
        
        // Dairy
        if (lowerIngredient.contains("milk")) return "🥛";
        if (lowerIngredient.contains("cheese")) return "🧀";
        if (lowerIngredient.contains("yogurt")) return "🥛";
        if (lowerIngredient.contains("butter")) return "🧈";
        if (lowerIngredient.contains("cream")) return "🥛";
        if (lowerIngredient.contains("sour cream")) return "🥛";
        if (lowerIngredient.contains("cottage cheese")) return "🧀";
        if (lowerIngredient.contains("ricotta")) return "🧀";
        if (lowerIngredient.contains("mozzarella")) return "🧀";
        if (lowerIngredient.contains("cheddar")) return "🧀";
        if (lowerIngredient.contains("parmesan")) return "🧀";
        if (lowerIngredient.contains("feta")) return "🧀";
        if (lowerIngredient.contains("goat cheese")) return "🧀";
        if (lowerIngredient.contains("brie")) return "🧀";
        if (lowerIngredient.contains("camembert")) return "🧀";
        
        // Legumes & Nuts
        if (lowerIngredient.contains("bean")) return "🥫";
        if (lowerIngredient.contains("lentil")) return "🟤";
        if (lowerIngredient.contains("chickpea")) return "🟡";
        if (lowerIngredient.contains("garbanzo")) return "🟡";
        if (lowerIngredient.contains("black bean")) return "⚫";
        if (lowerIngredient.contains("kidney bean")) return "🔴";
        if (lowerIngredient.contains("pinto bean")) return "🟤";
        if (lowerIngredient.contains("navy bean")) return "⚪";
        if (lowerIngredient.contains("lima bean")) return "🟢";
        if (lowerIngredient.contains("soybean")) return "🟡";
        if (lowerIngredient.contains("almond")) return "🌰";
        if (lowerIngredient.contains("walnut")) return "🌰";
        if (lowerIngredient.contains("pecan")) return "🌰";
        if (lowerIngredient.contains("cashew")) return "🌰";
        if (lowerIngredient.contains("pistachio")) return "🌰";
        if (lowerIngredient.contains("hazelnut")) return "🌰";
        if (lowerIngredient.contains("macadamia")) return "🌰";
        if (lowerIngredient.contains("brazil nut")) return "🌰";
        if (lowerIngredient.contains("pine nut")) return "🌰";
        if (lowerIngredient.contains("peanut")) return "🥜";
        if (lowerIngredient.contains("sunflower seed")) return "🌻";
        if (lowerIngredient.contains("pumpkin seed")) return "🎃";
        if (lowerIngredient.contains("sesame seed")) return "⚪";
        if (lowerIngredient.contains("chia seed")) return "⚫";
        if (lowerIngredient.contains("flax seed")) return "🟤";
        
        // Herbs & Spices
        if (lowerIngredient.contains("basil")) return "🌿";
        if (lowerIngredient.contains("oregano")) return "🌿";
        if (lowerIngredient.contains("thyme")) return "🌿";
        if (lowerIngredient.contains("rosemary")) return "🌿";
        if (lowerIngredient.contains("sage")) return "🌿";
        if (lowerIngredient.contains("mint")) return "🌿";
        if (lowerIngredient.contains("cilantro")) return "🌿";
        if (lowerIngredient.contains("parsley")) return "🌿";
        if (lowerIngredient.contains("dill")) return "🌿";
        if (lowerIngredient.contains("tarragon")) return "🌿";
        if (lowerIngredient.contains("bay leaf")) return "🍃";
        if (lowerIngredient.contains("cinnamon")) return "🟤";
        if (lowerIngredient.contains("nutmeg")) return "🟤";
        if (lowerIngredient.contains("clove")) return "🟤";
        if (lowerIngredient.contains("cardamom")) return "🟢";
        if (lowerIngredient.contains("cumin")) return "🟤";
        if (lowerIngredient.contains("coriander")) return "🟤";
        if (lowerIngredient.contains("paprika")) return "🔴";
        if (lowerIngredient.contains("turmeric")) return "🟡";
        if (lowerIngredient.contains("saffron")) return "🟡";
        if (lowerIngredient.contains("vanilla")) return "🟤";
        if (lowerIngredient.contains("black pepper")) return "⚫";
        if (lowerIngredient.contains("white pepper")) return "⚪";
        if (lowerIngredient.contains("cayenne")) return "🔴";
        if (lowerIngredient.contains("mustard seed")) return "🟡";
        if (lowerIngredient.contains("fennel seed")) return "🟢";
        if (lowerIngredient.contains("star anise")) return "⭐";
        
        // Oils & Condiments
        if (lowerIngredient.contains("olive oil")) return "🫒";
        if (lowerIngredient.contains("coconut oil")) return "🥥";
        if (lowerIngredient.contains("vegetable oil")) return "🟡";
        if (lowerIngredient.contains("canola oil")) return "🟡";
        if (lowerIngredient.contains("sesame oil")) return "🟤";
        if (lowerIngredient.contains("vinegar")) return "🍶";
        if (lowerIngredient.contains("balsamic")) return "🟤";
        if (lowerIngredient.contains("soy sauce")) return "🟤";
        if (lowerIngredient.contains("worcestershire")) return "🟤";
        if (lowerIngredient.contains("hot sauce")) return "🌶️";
        if (lowerIngredient.contains("ketchup")) return "🍅";
        if (lowerIngredient.contains("mustard")) return "🟡";
        if (lowerIngredient.contains("mayonnaise")) return "⚪";
        if (lowerIngredient.contains("honey")) return "🍯";
        if (lowerIngredient.contains("maple syrup")) return "🍁";
        if (lowerIngredient.contains("molasses")) return "🟤";
        if (lowerIngredient.contains("jam")) return "🍓";
        if (lowerIngredient.contains("jelly")) return "🍇";
        if (lowerIngredient.contains("peanut butter")) return "🥜";
        if (lowerIngredient.contains("almond butter")) return "🌰";
        if (lowerIngredient.contains("nutella")) return "🟤";
        
        // Beverages
        if (lowerIngredient.contains("coffee")) return "☕";
        if (lowerIngredient.contains("tea")) return "🍵";
        if (lowerIngredient.contains("juice")) return "🧃";
        if (lowerIngredient.contains("wine")) return "🍷";
        if (lowerIngredient.contains("beer")) return "🍺";
        if (lowerIngredient.contains("vodka")) return "🍸";
        if (lowerIngredient.contains("rum")) return "🥃";
        if (lowerIngredient.contains("whiskey")) return "🥃";
        if (lowerIngredient.contains("brandy")) return "🥃";
        if (lowerIngredient.contains("coconut water")) return "🥥";
        if (lowerIngredient.contains("soda")) return "🥤";
        if (lowerIngredient.contains("sparkling water")) return "🫧";
        
        // Sweets & Desserts
        if (lowerIngredient.contains("chocolate")) return "🍫";
        if (lowerIngredient.contains("cocoa")) return "🍫";
        if (lowerIngredient.contains("sugar")) return "🧂";
        if (lowerIngredient.contains("brown sugar")) return "🟤";
        if (lowerIngredient.contains("powdered sugar")) return "⚪";
        if (lowerIngredient.contains("vanilla extract")) return "🟤";
        if (lowerIngredient.contains("baking powder")) return "⚪";
        if (lowerIngredient.contains("baking soda")) return "⚪";
        if (lowerIngredient.contains("yeast")) return "🟡";
        if (lowerIngredient.contains("cornstarch")) return "⚪";
        if (lowerIngredient.contains("gelatin")) return "⚪";
        if (lowerIngredient.contains("marshmallow")) return "⚪";
        if (lowerIngredient.contains("coconut flake")) return "⚪";
        if (lowerIngredient.contains("raisin")) return "🟤";
        if (lowerIngredient.contains("date")) return "🟤";
        if (lowerIngredient.contains("fig")) return "🟣";
        if (lowerIngredient.contains("cranberry")) return "🔴";
        
        // Frozen Foods
        if (lowerIngredient.contains("ice cream")) return "🍨";
        if (lowerIngredient.contains("frozen yogurt")) return "🍦";
        if (lowerIngredient.contains("sorbet")) return "🍧";
        if (lowerIngredient.contains("popsicle")) return "🍭";
        
        // Canned/Processed
        if (lowerIngredient.contains("tomato sauce")) return "🍅";
        if (lowerIngredient.contains("tomato paste")) return "🍅";
        if (lowerIngredient.contains("coconut milk")) return "🥥";
        if (lowerIngredient.contains("almond milk")) return "🌰";
        if (lowerIngredient.contains("oat milk")) return "🥣";
        if (lowerIngredient.contains("soy milk")) return "🟡";
        if (lowerIngredient.contains("broth")) return "🍲";
        if (lowerIngredient.contains("stock")) return "🍲";
        
        return "🥗"; // Default emoji for other ingredients
    }
}
