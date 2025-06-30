package com.vedakunamneni.click.models;

import java.util.List;

public class Recipe {
    private int id;
    private String name;
    private String description;
    private int cookingTimeMinutes;
    private int difficulty; // 1-5 scale
    private String category;
    private String color; // CSS color for the card
    private List<String> ingredients;
    private List<String> instructions;
    private String imageUrl;
    private int servings;
    
    public Recipe(int id, String name, String description, int cookingTimeMinutes, 
                  int difficulty, String category, String color, List<String> ingredients, 
                  List<String> instructions, String imageUrl, int servings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.category = category;
        this.color = color;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.servings = servings;
    }
    
    public Recipe(String name, String description, int cookingTimeMinutes, 
                  int difficulty, String category, String color, List<String> ingredients, 
                  List<String> instructions, int servings) {
        this.name = name;
        this.description = description;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.difficulty = difficulty;
        this.category = category;
        this.color = color;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.servings = servings;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    // Helper method to set ID from String (for API responses)
    public void setId(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            // Generate a random ID if the string is null or empty
            this.id = (int) (Math.random() * 1000000);
            return;
        }
        
        try {
            this.id = Integer.parseInt(idString.trim());
        } catch (NumberFormatException e) {
            // If the ID is not a valid integer, use hashCode of the string
            this.id = idString.hashCode();
        }
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getCookingTimeMinutes() { return cookingTimeMinutes; }
    public void setCookingTimeMinutes(int cookingTimeMinutes) { this.cookingTimeMinutes = cookingTimeMinutes; }
    
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public int getServings() { return servings; }
    
    public String getCookingTimeFormatted() {
        if (cookingTimeMinutes < 60) {
            return cookingTimeMinutes + " min";
        } else {
            int hours = cookingTimeMinutes / 60;
            int mins = cookingTimeMinutes % 60;
            if (mins == 0) {
                return hours + "h";
            } else {
                return hours + "h " + mins + "m";
            }
        }
    }
    
    public String getDifficultyStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= difficulty) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    public String getDifficultyText() {
        switch (difficulty) {
            case 1: return "Very Easy";
            case 2: return "Easy";
            case 3: return "Medium";
            case 4: return "Hard";
            case 5: return "Very Hard";
            default: return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return name + " (" + getCookingTimeFormatted() + ", " + getDifficultyText() + ")";
    }
}
