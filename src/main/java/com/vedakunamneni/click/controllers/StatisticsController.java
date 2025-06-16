package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.click.models.Ingredient;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class StatisticsController {

    @FXML
    private Label foodSavedLabel;
    
    @FXML
    private Label foodWastedLabel;
    
    @FXML
    private Label totalItemsLabel;
    
    @FXML
    private Label efficiencyScoreLabel;
    
    @FXML
    private ProgressBar efficiencyProgressBar;
    
    @FXML
    private Label efficiencyMessageLabel;
    
    @FXML
    private Label expiringSoonLabel;
    
    @FXML
    private Label addedThisWeekLabel;
    
    @FXML
    private Label avgDaysToExpirationLabel;
    
    @FXML
    private Label mostWastedCategoryLabel;
    
    @FXML
    private VBox tipsContainer;

    @FXML
    private void initialize() {
        loadStatistics();
    }

    private void loadStatistics() {
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) {
            return;
        }

        List<Ingredient> inventory = DatabaseHelper.getUserInventory(userEmail);
        
        // Calculate statistics
        int totalItems = inventory.size();
        int foodSaved = calculateFoodSaved(inventory);
        int foodWasted = calculateFoodWasted(inventory);
        int expiringSoon = calculateExpiringSoon(inventory);
        int addedThisWeek = calculateAddedThisWeek(inventory);
        double avgDaysToExpiration = calculateAvgDaysToExpiration(inventory);
        double efficiencyScore = calculateEfficiencyScore(foodSaved, foodWasted);
        
        // Update UI labels
        if (totalItemsLabel != null) {
            totalItemsLabel.setText(String.valueOf(totalItems));
        }
        
        if (foodSavedLabel != null) {
            foodSavedLabel.setText(String.valueOf(foodSaved));
        }
        
        if (foodWastedLabel != null) {
            foodWastedLabel.setText(String.valueOf(foodWasted));
        }
        
        if (expiringSoonLabel != null) {
            expiringSoonLabel.setText(String.valueOf(expiringSoon));
        }
        
        if (addedThisWeekLabel != null) {
            addedThisWeekLabel.setText(String.valueOf(addedThisWeek));
        }
        
        if (avgDaysToExpirationLabel != null) {
            avgDaysToExpirationLabel.setText(String.format("%.1f days", avgDaysToExpiration));
        }
        
        if (efficiencyScoreLabel != null) {
            efficiencyScoreLabel.setText(String.format("%.0f%%", efficiencyScore));
        }
        
        if (efficiencyProgressBar != null) {
            efficiencyProgressBar.setProgress(efficiencyScore / 100.0);
        }
        
        if (efficiencyMessageLabel != null) {
            efficiencyMessageLabel.setText(getEfficiencyMessage(efficiencyScore));
        }
        
        if (mostWastedCategoryLabel != null) {
            mostWastedCategoryLabel.setText(getMostWastedCategory(inventory));
        }
        
        loadTips(efficiencyScore, expiringSoon);
    }

    private int calculateFoodSaved(List<Ingredient> inventory) {
        // Count items that were likely consumed before expiring
        return (int) inventory.stream()
            .filter(item -> item.getDaysUntilExpiration() >= 0)
            .count();
    }

    private int calculateFoodWasted(List<Ingredient> inventory) {
        // Count expired items
        return (int) inventory.stream()
            .filter(item -> item.getDaysUntilExpiration() < 0)
            .count();
    }

    private int calculateExpiringSoon(List<Ingredient> inventory) {
        // Count items expiring in the next 3 days
        return (int) inventory.stream()
            .filter(item -> {
                long days = item.getDaysUntilExpiration();
                return days >= 0 && days <= 3;
            })
            .count();
    }

    private int calculateAddedThisWeek(List<Ingredient> inventory) {
        LocalDate weekAgo = LocalDate.now().minusWeeks(1);
        return (int) inventory.stream()
            .filter(item -> item.getDateAdded().isAfter(weekAgo))
            .count();
    }

    private double calculateAvgDaysToExpiration(List<Ingredient> inventory) {
        if (inventory.isEmpty()) {
            return 0.0;
        }
        
        double totalDays = inventory.stream()
            .filter(item -> item.getDaysUntilExpiration() >= 0)
            .mapToLong(Ingredient::getDaysUntilExpiration)
            .average()
            .orElse(0.0);
        
        return totalDays;
    }

    private double calculateEfficiencyScore(int foodSaved, int foodWasted) {
        int total = foodSaved + foodWasted;
        if (total == 0) {
            return 100.0; // Perfect score if no data
        }
        return (double) foodSaved / total * 100.0;
    }

    private String getEfficiencyMessage(double score) {
        if (score >= 90) {
            return "Excellent! You're a food waste warrior!";
        } else if (score >= 75) {
            return "Great job! Keep up the good work!";
        } else if (score >= 50) {
            return "Good progress! Room for improvement.";
        } else {
            return "Keep tracking to reduce waste!";
        }
    }

    private String getMostWastedCategory(List<Ingredient> inventory) {
        Map<String, Long> wastedByCategory = inventory.stream()
            .filter(item -> item.getDaysUntilExpiration() < 0)
            .collect(Collectors.groupingBy(
                item -> getCategoryFromName(item.getName()),
                Collectors.counting()
            ));
        
        return wastedByCategory.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
    }

    private String getCategoryFromName(String name) {
        // Simple categorization based on common food names
        String lowerName = name.toLowerCase();
        if (lowerName.contains("milk") || lowerName.contains("cheese") || lowerName.contains("yogurt")) {
            return "Dairy";
        } else if (lowerName.contains("apple") || lowerName.contains("banana") || lowerName.contains("orange") || 
                   lowerName.contains("berry") || lowerName.contains("fruit")) {
            return "Fruits";
        } else if (lowerName.contains("lettuce") || lowerName.contains("spinach") || lowerName.contains("carrot") ||
                   lowerName.contains("tomato") || lowerName.contains("vegetable")) {
            return "Vegetables";
        } else if (lowerName.contains("chicken") || lowerName.contains("beef") || lowerName.contains("fish") ||
                   lowerName.contains("meat")) {
            return "Meat";
        } else {
            return "Other";
        }
    }

    private void loadTips(double efficiencyScore, int expiringSoon) {
        if (tipsContainer == null) {
            return;
        }
        
        tipsContainer.getChildren().clear();
        
        if (expiringSoon > 0) {
            Label tip1 = new Label("• You have " + expiringSoon + " items expiring soon. Use them first!");
            tip1.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            tipsContainer.getChildren().add(tip1);
        }
        
        if (efficiencyScore < 75) {
            Label tip2 = new Label("• Set up expiration date reminders to use items before they spoil.");
            tip2.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            tipsContainer.getChildren().add(tip2);
        }
        
        Label tip3 = new Label("• Store fruits and vegetables separately to prevent premature ripening.");
        tip3.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        tipsContainer.getChildren().add(tip3);
        
        Label tip4 = new Label("• Use the 'First In, First Out' method - consume older items before newer ones.");
        tip4.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        tipsContainer.getChildren().add(tip4);
        
        Label tip5 = new Label("• Check your pantry regularly and plan meals around expiring ingredients.");
        tip5.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        tipsContainer.getChildren().add(tip5);
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
    private void goToScanner() throws IOException {
        App.setRoot("scanner");
    }

    @FXML
    private void goToRecipes() throws IOException {
        App.setRoot("recipe");
    }

    @FXML
    private void logout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
    }
}