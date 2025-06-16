package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.time.LocalTime;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label tipLabel;

    @FXML
    private ProgressBar ecoProgressBar;

    @FXML
    private Label ecoPointsLabel;

    @FXML
    private VBox expiringSoonList;

    @FXML
    private void initialize() {
        // Get real user points and level from database
        String userEmail = SessionManager.getCurrentUser();
        int[] pointsAndLevel = DatabaseHelper.getUserPointsAndLevel(userEmail);
        int currentPoints = pointsAndLevel[0];
        int currentLevel = pointsAndLevel[1];
        int pointsForNextLevel = DatabaseHelper.getPointsForNextLevel(currentPoints);
        
        // Calculate progress for next level
        int currentLevelStartPoints = getLevelStartPoints(currentLevel);
        int nextLevelPoints = currentLevelStartPoints + pointsForNextLevel;
        double progress = nextLevelPoints > currentLevelStartPoints ? 
            (double)(currentPoints - currentLevelStartPoints) / (nextLevelPoints - currentLevelStartPoints) : 1.0;

        if (welcomeLabel != null) {
            String greeting = getTimeBasedGreeting();
            String displayName = SessionManager.getDisplayName();
            welcomeLabel.setText(greeting + ", " + displayName + "!");
        }
        if (tipLabel != null) {
            tipLabel.setText("Revive wilted greens by soaking them in ice water for 30 minutes.");
        }
        if (ecoProgressBar != null) {
            ecoProgressBar.setProgress(Math.min(progress, 1.0));
        }
        if (ecoPointsLabel != null) {
            if (currentLevel >= 10 && pointsForNextLevel <= 0) {
                ecoPointsLabel.setText(currentPoints + " Points | Level " + currentLevel + " (Max Level!)");
            } else {
                ecoPointsLabel.setText(currentPoints + " Points | Level " + currentLevel + " | " + pointsForNextLevel + " more to level up!");
            }
        }
    }
    
    private int getLevelStartPoints(int level) {
        // Return the minimum points needed for this level
        switch (level) {
            case 1: return 0;
            case 2: return 100;
            case 3: return 300;
            case 4: return 600;
            case 5: return 1000;
            case 6: return 1500;
            case 7: return 2100;
            case 8: return 2800;
            case 9: return 3600;
            case 10: return 4500;
            default: return 5500 + (level - 11) * 1000;
        }
    }

    private String getTimeBasedGreeting() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(12, 0))) {
            return "Good Morning";
        } else if (now.isBefore(LocalTime.of(17, 0))) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
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
    private void goToStatistics() throws IOException {
        App.setRoot("statistics");
    }
}
