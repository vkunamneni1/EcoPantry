package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;

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
        double currentPoints = 1247.0;

        if (welcomeLabel != null) {
            welcomeLabel.setText("Good Morning, Veda!");
        }
        if (tipLabel != null) {
            tipLabel.setText("Revive wilted greens by soaking them in ice water for 30 minutes.");
        }
        if (ecoProgressBar != null) {
            ecoProgressBar.setProgress(currentPoints/1697);
        }
        if (ecoPointsLabel != null) {
            ecoPointsLabel.setText((int) currentPoints + " Points | " + (1697 - (int) currentPoints) + " more to the next level!");
        }
    }

    @FXML
    private void handleLogout() throws IOException {
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
}
