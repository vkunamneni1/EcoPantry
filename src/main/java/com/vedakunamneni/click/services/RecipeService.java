package com.vedakunamneni.click.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.click.models.Recipe;

public class RecipeService {
    
    private static List<Recipe> allRecipes = new ArrayList<>();
    
    static {
        initializeRecipes();
    }
    
    private static void initializeRecipes() {
        // Vegetable-based recipes
        allRecipes.add(new Recipe(
            "Roasted Vegetable Medley",
            "A colorful mix of roasted vegetables with herbs and olive oil",
            35, 2, "Vegetarian", "#4CAF50",
            Arrays.asList("Carrots", "Broccoli", "Bell Peppers", "Zucchini", "Olive Oil", "Salt", "Pepper", "Garlic"),
            Arrays.asList(
                "Preheat oven to 425°F (220°C)",
                "Wash and chop all vegetables into bite-sized pieces",
                "Toss vegetables with olive oil, salt, pepper, and minced garlic",
                "Spread on a baking sheet in a single layer",
                "Roast for 25-30 minutes until tender and lightly browned",
                "Serve hot as a side dish or over rice"
            ),
            4
        ));
        
        allRecipes.add(new Recipe(
            "Fresh Garden Salad",
            "A light and refreshing salad with mixed greens",
            10, 1, "Salad", "#81C784",
            Arrays.asList("Spinach", "Tomatoes", "Carrots", "Onions", "Olive Oil", "Lemon", "Salt"),
            Arrays.asList(
                "Wash and dry all greens thoroughly",
                "Chop tomatoes and carrots into small pieces",
                "Thinly slice onions",
                "Combine all vegetables in a large bowl",
                "Whisk together olive oil, lemon juice, and salt for dressing",
                "Toss salad with dressing just before serving"
            ),
            2
        ));
        
        allRecipes.add(new Recipe(
            "Creamy Mushroom Pasta",
            "Rich and creamy pasta with sautéed mushrooms",
            25, 3, "Pasta", "#FF9800",
            Arrays.asList("Mushrooms", "Garlic", "Onions", "Heavy Cream", "Pasta", "Cheese", "Butter", "Salt", "Pepper"),
            Arrays.asList(
                "Cook pasta according to package directions",
                "Slice mushrooms and onions",
                "Sauté mushrooms in butter until golden",
                "Add onions and garlic, cook until fragrant",
                "Pour in cream and simmer for 5 minutes",
                "Add cooked pasta and cheese, toss to combine",
                "Season with salt and pepper to taste"
            ),
            3
        ));
        
        // Protein-based recipes
        allRecipes.add(new Recipe(
            "Herb-Crusted Chicken Breast",
            "Juicy chicken breast with a flavorful herb crust",
            30, 3, "Protein", "#F44336",
            Arrays.asList("Chicken Breast", "Garlic", "Herbs", "Olive Oil", "Salt", "Pepper", "Lemon"),
            Arrays.asList(
                "Preheat oven to 375°F (190°C)",
                "Pound chicken breasts to even thickness",
                "Mix minced garlic, herbs, olive oil, salt, and pepper",
                "Rub herb mixture all over chicken",
                "Place in baking dish and bake for 20-25 minutes",
                "Check internal temperature reaches 165°F (74°C)",
                "Let rest 5 minutes before slicing, serve with lemon"
            ),
            4
        ));
        
        allRecipes.add(new Recipe(
            "Scrambled Eggs with Vegetables",
            "Fluffy scrambled eggs with fresh vegetables",
            15, 2, "Breakfast", "#FFEB3B",
            Arrays.asList("Eggs", "Bell Peppers", "Onions", "Spinach", "Cheese", "Butter", "Salt", "Pepper"),
            Arrays.asList(
                "Crack eggs into a bowl and whisk with salt and pepper",
                "Dice bell peppers and onions",
                "Heat butter in a non-stick pan over medium heat",
                "Add vegetables and cook until softened",
                "Pour in eggs and gently scramble",
                "Add spinach in the last minute",
                "Top with cheese and serve immediately"
            ),
            2
        ));
        
        // Fruit-based recipes
        allRecipes.add(new Recipe(
            "Fresh Fruit Smoothie",
            "A refreshing and healthy fruit smoothie",
            5, 1, "Beverage", "#E91E63",
            Arrays.asList("Bananas", "Apples", "Milk", "Honey", "Ice"),
            Arrays.asList(
                "Peel and slice bananas",
                "Core and chop apples",
                "Add all fruits to blender",
                "Pour in milk and add honey to taste",
                "Add ice cubes",
                "Blend until smooth and creamy",
                "Serve immediately in chilled glasses"
            ),
            2
        ));
        
        // Bread-based recipes
        allRecipes.add(new Recipe(
            "Avocado Toast",
            "Simple and healthy avocado toast with seasonings",
            8, 1, "Breakfast", "#4CAF50",
            Arrays.asList("Bread", "Avocado", "Lemon", "Salt", "Pepper", "Tomatoes"),
            Arrays.asList(
                "Toast bread slices until golden brown",
                "Mash ripe avocado in a bowl",
                "Add lemon juice, salt, and pepper to avocado",
                "Spread avocado mixture on toast",
                "Top with sliced tomatoes if desired",
                "Serve immediately"
            ),
            1
        ));
        
        // Complex recipes
        allRecipes.add(new Recipe(
            "Vegetable Stir Fry",
            "Quick and colorful vegetable stir fry with Asian flavors",
            20, 2, "Asian", "#9C27B0",
            Arrays.asList("Broccoli", "Carrots", "Bell Peppers", "Onions", "Garlic", "Soy Sauce", "Oil", "Ginger"),
            Arrays.asList(
                "Cut all vegetables into uniform pieces",
                "Heat oil in a large wok or skillet over high heat",
                "Add garlic and ginger, stir fry for 30 seconds",
                "Add harder vegetables (carrots, broccoli) first",
                "Stir fry for 2-3 minutes, then add softer vegetables",
                "Add soy sauce and toss everything together",
                "Cook for another 2 minutes until vegetables are crisp-tender",
                "Serve hot over rice"
            ),
            3
        ));
        
        allRecipes.add(new Recipe(
            "Cheese Omelet",
            "Classic fluffy omelet filled with melted cheese",
            12, 2, "Breakfast", "#FFC107",
            Arrays.asList("Eggs", "Cheese", "Butter", "Salt", "Pepper", "Chives"),
            Arrays.asList(
                "Beat eggs with salt and pepper in a bowl",
                "Heat butter in a non-stick pan over medium-low heat",
                "Pour in eggs and let set for 30 seconds",
                "Gently stir eggs, pulling edges to center",
                "When eggs are almost set, add cheese to one half",
                "Fold omelet in half and slide onto plate",
                "Garnish with chopped chives and serve"
            ),
            1
        ));
        
        allRecipes.add(new Recipe(
            "Roasted Sweet Potato",
            "Simple roasted sweet potato with herbs",
            45, 1, "Side Dish", "#FF5722",
            Arrays.asList("Sweet Potatoes", "Olive Oil", "Salt", "Pepper", "Herbs"),
            Arrays.asList(
                "Preheat oven to 400°F (200°C)",
                "Wash and pierce sweet potatoes with a fork",
                "Rub with olive oil and season with salt and pepper",
                "Place on baking sheet",
                "Roast for 35-45 minutes until tender",
                "Sprinkle with fresh herbs before serving"
            ),
            4
        ));
    }
    
    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(allRecipes);
    }
    
    public static List<Recipe> getRecipesForIngredients(List<Ingredient> userIngredients) {
        List<String> userIngredientNames = userIngredients.stream()
            .map(ingredient -> ingredient.getName().toLowerCase())
            .collect(Collectors.toList());
        
        return allRecipes.stream()
            .filter(recipe -> {
                // Check how many recipe ingredients the user has
                long matchingIngredients = recipe.getIngredients().stream()
                    .filter(recipeIngredient -> 
                        userIngredientNames.stream()
                            .anyMatch(userIngredient -> 
                                userIngredient.contains(recipeIngredient.toLowerCase()) ||
                                recipeIngredient.toLowerCase().contains(userIngredient)
                            )
                    )
                    .count();
                
                // Return recipes where user has at least 50% of ingredients
                return matchingIngredients >= Math.max(1, recipe.getIngredients().size() * 0.5);
            })
            .sorted((r1, r2) -> {
                // Sort by number of matching ingredients (descending)
                long matches1 = getMatchingIngredientsCount(r1, userIngredientNames);
                long matches2 = getMatchingIngredientsCount(r2, userIngredientNames);
                return Long.compare(matches2, matches1);
            })
            .collect(Collectors.toList());
    }
    
    private static long getMatchingIngredientsCount(Recipe recipe, List<String> userIngredientNames) {
        return recipe.getIngredients().stream()
            .filter(recipeIngredient -> 
                userIngredientNames.stream()
                    .anyMatch(userIngredient -> 
                        userIngredient.contains(recipeIngredient.toLowerCase()) ||
                        recipeIngredient.toLowerCase().contains(userIngredient)
                    )
            )
            .count();
    }
    
    public static Recipe getRecipeById(int id) {
        return allRecipes.stream()
            .filter(recipe -> recipe.getId() == id)
            .findFirst()
            .orElse(null);
    }
    
    public static List<Recipe> getRecipesByCategory(String category) {
        return allRecipes.stream()
            .filter(recipe -> recipe.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
    
    public static List<Recipe> getRecipesByDifficulty(int maxDifficulty) {
        return allRecipes.stream()
            .filter(recipe -> recipe.getDifficulty() <= maxDifficulty)
            .collect(Collectors.toList());
    }
    
    public static List<Recipe> getQuickRecipes(int maxMinutes) {
        return allRecipes.stream()
            .filter(recipe -> recipe.getCookingTimeMinutes() <= maxMinutes)
            .collect(Collectors.toList());
    }
}
