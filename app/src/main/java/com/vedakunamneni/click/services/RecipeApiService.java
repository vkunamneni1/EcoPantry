package com.vedakunamneni.click.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;

public class RecipeApiService {
    
    private static final String MEAL_DB_BASE_URL = "https://www.themealdb.com/api/json/v1/1";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    /**
     * Search for recipes by ingredient
     */
    public static CompletableFuture<List<Recipe>> searchRecipesByIngredient(String ingredient) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = MEAL_DB_BASE_URL + "/filter.php?i=" + ingredient.toLowerCase().replace(" ", "%20");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return parseRecipesFromJson(response.body());
                }
            } catch (Exception e) {
                System.err.println("Error fetching recipes by ingredient: " + e.getMessage());
            }
            return new ArrayList<>();
        });
    }
    
    /**
     * Get random recipes
     */
    public static CompletableFuture<List<Recipe>> getRandomRecipes(int count) {
        return CompletableFuture.supplyAsync(() -> {
            List<Recipe> recipes = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                try {
                    String url = MEAL_DB_BASE_URL + "/random.php";
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(10))
                            .GET()
                            .build();
                    
                    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        List<Recipe> randomRecipes = parseRecipesFromJson(response.body());
                        if (!randomRecipes.isEmpty()) {
                            recipes.add(randomRecipes.get(0));
                        }
                    }
                    
                    // Small delay to avoid hitting API too quickly
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.err.println("Error fetching random recipe: " + e.getMessage());
                }
            }
            
            return recipes;
        });
    }
    
    /**
     * Get recipe details by ID
     */
    public static CompletableFuture<Recipe> getRecipeById(String mealId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = MEAL_DB_BASE_URL + "/lookup.php?i=" + mealId;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    List<Recipe> recipes = parseRecipesFromJson(response.body());
                    return recipes.isEmpty() ? null : recipes.get(0);
                }
            } catch (Exception e) {
                System.err.println("Error fetching recipe by ID: " + e.getMessage());
            }
            return null;
        });
    }
    
    /**
     * Search recipes based on user's available ingredients
     */
    public static CompletableFuture<List<Recipe>> getRecipesForIngredients(List<Ingredient> userIngredients) {
        if (userIngredients.isEmpty()) {
            return getRandomRecipes(10);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            List<Recipe> allRecipes = new ArrayList<>();
            
            // Search by each ingredient the user has
            for (Ingredient ingredient : userIngredients) {
                try {
                    CompletableFuture<List<Recipe>> future = searchRecipesByIngredient(ingredient.getName());
                    List<Recipe> recipes = future.get();
                    allRecipes.addAll(recipes);
                    
                    // Limit to avoid too many API calls
                    if (allRecipes.size() >= 20) break;
                } catch (Exception e) {
                    System.err.println("Error getting recipes for ingredient " + ingredient.getName() + ": " + e.getMessage());
                }
            }
            
            // Remove duplicates and limit results
            return allRecipes.stream()
                    .distinct()
                    .limit(15)
                    .collect(ArrayList::new, (list, recipe) -> list.add(recipe), ArrayList::addAll);
        });
    }
    
    /**
     * Parse JSON response from TheMealDB API
     */
    private static List<Recipe> parseRecipesFromJson(String jsonResponse) {
        List<Recipe> recipes = new ArrayList<>();
        
        try {
            // Simple JSON parsing without external libraries
            if (jsonResponse.contains("\"meals\":null")) {
                return recipes;
            }
            
            // Split by meal objects
            String[] mealSections = jsonResponse.split("\"idMeal\":");
            
            for (int i = 1; i < mealSections.length; i++) {
                String mealSection = mealSections[i];
                
                try {
                    // Extract basic information
                    String id = extractJsonValue(mealSection, "idMeal", true);
                    String name = extractJsonValue(mealSection, "strMeal", false);
                    String category = extractJsonValue(mealSection, "strCategory", false);
                    String area = extractJsonValue(mealSection, "strArea", false);
                    String instructions = extractJsonValue(mealSection, "strInstructions", false);
                    String image = extractJsonValue(mealSection, "strMealThumb", false);
                    
                    // Skip if essential fields are missing
                    if (name == null || name.trim().isEmpty()) {
                        continue;
                    }
                    
                    // Extract ingredients
                    List<String> ingredients = new ArrayList<>();
                    for (int j = 1; j <= 20; j++) {
                        String ingredient = extractJsonValue(mealSection, "strIngredient" + j, false);
                        String measure = extractJsonValue(mealSection, "strMeasure" + j, false);
                        
                        if (ingredient != null && !ingredient.trim().isEmpty()) {
                            if (measure != null && !measure.trim().isEmpty()) {
                                ingredients.add(measure.trim() + " " + ingredient.trim());
                            } else {
                                ingredients.add(ingredient.trim());
                            }
                        }
                    }
                    
                    // Skip recipes with no ingredients
                    if (ingredients.isEmpty()) {
                        continue;
                    }
                    
                    // Parse instructions into steps
                    List<String> steps = new ArrayList<>();
                    if (instructions != null && !instructions.isEmpty()) {
                        String[] instructionLines = instructions.split("\\r?\\n");
                        for (String line : instructionLines) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                steps.add(line);
                            }
                        }
                        
                        // If no line breaks, try splitting by periods or numbers
                        if (steps.size() <= 1 && instructions.length() > 50) {
                            steps.clear();
                            String[] sentences = instructions.split("\\. ");
                            for (String sentence : sentences) {
                                sentence = sentence.trim();
                                if (!sentence.isEmpty()) {
                                    if (!sentence.endsWith(".")) {
                                        sentence += ".";
                                    }
                                    steps.add(sentence);
                                }
                            }
                        }
                    }
                    
                    if (steps.isEmpty()) {
                        steps.add("Instructions not available. Please refer to the original source.");
                    }
                    
                    // Create recipe with estimated values
                    String description = (area != null ? area + " " : "") + (category != null ? category : "Recipe");
                    int cookTime = estimateCookTime(instructions, ingredients.size());
                    int difficulty = estimateDifficulty(instructions, ingredients.size());
                    String color = getCategoryColor(category);
                    int rating = 4; // Default rating
                    
                    Recipe recipe = new Recipe(name, description, cookTime, difficulty, category != null ? category : "Other", color, ingredients, steps, rating);
                    
                    // Set ID - this is now null-safe
                    recipe.setId(id);
                    
                    // Set image URL if available
                    if (image != null && !image.trim().isEmpty()) {
                        recipe.setImageUrl(image);
                    }
                    
                    recipes.add(recipe);
                    
                } catch (Exception e) {
                    System.err.println("Error parsing individual recipe: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
        }
        
        return recipes;
    }
    
    private static String extractJsonValue(String json, String key, boolean isNumber) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            
            if (startIndex == -1) {
                return null;
            }
            
            startIndex += searchKey.length();
            
            if (isNumber) {
                // For numbers, find the next comma or closing brace
                int endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = json.indexOf("}", startIndex);
                }
                if (endIndex == -1) {
                    endIndex = json.length();
                }
                
                return json.substring(startIndex, endIndex).trim().replaceAll("\"", "");
            } else {
                // For strings, find the closing quote
                if (json.charAt(startIndex) == '"') {
                    startIndex++; // Skip opening quote
                    int endIndex = startIndex;
                    
                    // Find closing quote, handling escaped quotes
                    while (endIndex < json.length()) {
                        if (json.charAt(endIndex) == '"' && (endIndex == 0 || json.charAt(endIndex - 1) != '\\')) {
                            break;
                        }
                        endIndex++;
                    }
                    
                    if (endIndex < json.length()) {
                        return json.substring(startIndex, endIndex);
                    }
                } else if (json.substring(startIndex).startsWith("null")) {
                    return null;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting JSON value for key " + key + ": " + e.getMessage());
        }
        
        return null;
    }
    
    private static int estimateCookTime(String instructions, int ingredientCount) {
        if (instructions == null) return 30;
        
        String lower = instructions.toLowerCase();
        
        // Look for time indicators
        if (lower.contains("microwave")) return 5;
        if (lower.contains("quick") || lower.contains("instant")) return 15;
        if (lower.contains("slow cook") || lower.contains("braise")) return 120;
        if (lower.contains("bake") || lower.contains("roast")) return 45;
        if (lower.contains("simmer") || lower.contains("stew")) return 60;
        
        // Estimate based on ingredient count and instruction length
        int baseTime = Math.max(15, ingredientCount * 3);
        int instructionComplexity = Math.min(30, instructions.length() / 50);
        
        return baseTime + instructionComplexity;
    }
    
    private static int estimateDifficulty(String instructions, int ingredientCount) {
        if (instructions == null) return 2;
        
        String lower = instructions.toLowerCase();
        int difficulty = 1;
        
        // Increase difficulty based on techniques
        if (lower.contains("chop") || lower.contains("dice") || lower.contains("slice")) difficulty++;
        if (lower.contains("sauté") || lower.contains("fry")) difficulty++;
        if (lower.contains("braise") || lower.contains("reduce")) difficulty++;
        if (lower.contains("fold") || lower.contains("whisk")) difficulty++;
        if (lower.contains("temper") || lower.contains("emulsify")) difficulty += 2;
        
        // Adjust based on ingredient count
        if (ingredientCount > 10) difficulty++;
        if (ingredientCount > 15) difficulty++;
        
        return Math.min(5, Math.max(1, difficulty));
    }
    
    private static String getCategoryColor(String category) {
        if (category == null) return "#FF9800";
        
        switch (category.toLowerCase()) {
            case "vegetarian": return "#4CAF50";
            case "vegan": return "#8BC34A";
            case "beef": return "#F44336";
            case "chicken": return "#FF9800";
            case "pork": return "#E91E63";
            case "seafood": return "#2196F3";
            case "lamb": return "#9C27B0";
            case "pasta": return "#FF5722";
            case "dessert": return "#FFEB3B";
            case "breakfast": return "#FFC107";
            case "side": return "#795548";
            case "starter": return "#607D8B";
            default: return "#FF9800";
        }
    }
}
