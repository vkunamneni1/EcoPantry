package com.vedakunamneni.click.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;

public class RecipeService {
    
    /**
     * Get recipes based on user's ingredients using external API
     */
    public static List<Recipe> getRecipesForIngredients(List<Ingredient> userIngredients) {
        try {
            CompletableFuture<List<Recipe>> future = RecipeApiService.getRecipesForIngredients(userIngredients);
            return future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching recipes from API: " + e.getMessage());
            return getFallbackRecipes(); // Return fallback recipes if API fails
        }
    }
    
    /**
     * Get random recipes using external API
     */
    public static List<Recipe> getRandomRecipes(int count) {
        try {
            CompletableFuture<List<Recipe>> future = RecipeApiService.getRandomRecipes(count);
            return future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching random recipes from API: " + e.getMessage());
            return getFallbackRecipes(); // Return fallback recipes if API fails
        }
    }
    
    /**
     * Search recipes by ingredient using external API
     */
    public static List<Recipe> searchRecipesByIngredient(String ingredient) {
        try {
            CompletableFuture<List<Recipe>> future = RecipeApiService.searchRecipesByIngredient(ingredient);
            return future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error searching recipes by ingredient from API: " + e.getMessage());
            return getFallbackRecipes(); // Return fallback recipes if API fails
        }
    }
    
    /**
     * Fallback recipes in case API is unavailable
     */
    private static List<Recipe> getFallbackRecipes() {
        List<Recipe> fallbackRecipes = new ArrayList<>();
        
        // Simple fallback recipes
        fallbackRecipes.add(new Recipe(
            "Simple Pasta",
            "Quick and easy pasta dish",
            15, 1, "Pasta", "#FF9800",
            Arrays.asList("Pasta", "Olive Oil", "Garlic", "Salt", "Pepper"),
            Arrays.asList(
                "Boil water and cook pasta according to package directions",
                "Heat olive oil in a pan and add minced garlic",
                "Drain pasta and toss with garlic oil",
                "Season with salt and pepper to taste"
            ),
            2
        ));
        
        fallbackRecipes.add(new Recipe(
            "Basic Salad",
            "Fresh mixed greens salad",
            10, 1, "Salad", "#4CAF50",
            Arrays.asList("Mixed Greens", "Tomatoes", "Cucumber", "Olive Oil", "Lemon"),
            Arrays.asList(
                "Wash and dry the greens",
                "Chop tomatoes and cucumber",
                "Combine vegetables in a bowl",
                "Drizzle with olive oil and lemon juice"
            ),
            2
        ));
        
        fallbackRecipes.add(new Recipe(
            "Scrambled Eggs",
            "Classic breakfast eggs",
            10, 1, "Breakfast", "#FFEB3B",
            Arrays.asList("Eggs", "Butter", "Salt", "Pepper"),
            Arrays.asList(
                "Crack eggs into a bowl and whisk",
                "Heat butter in a non-stick pan",
                "Pour in eggs and gently scramble",
                "Season with salt and pepper"
            ),
            1
        ));
        
        return fallbackRecipes;
    }
}
