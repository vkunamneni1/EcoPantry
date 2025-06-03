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
        if (welcomeLabel != null) {
            welcomeLabel.setText("Good Morning, Veda!");
        }
        if (tipLabel != null) {
            tipLabel.setText("Revive wilted greens by soaking them in ice water for 30 minutes.");
        }
        if (ecoProgressBar != null) {
            ecoProgressBar.setProgress(0.6);
        }
        if (ecoPointsLabel != null) {
            ecoPointsLabel.setText("1,247 Points | 450 more to the next level!");
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

}
