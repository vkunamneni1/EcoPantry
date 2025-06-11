package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

public class ScannerController {

    @FXML
    private VBox dropZone;

    @FXML
    private VBox ingredientsBox;

    @FXML
    public void initialize() {
        dropZone.setOnDragOver(e -> {
            if (e.getGestureSource() != dropZone && e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        dropZone.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                showMockIngredients();
            }
            e.setDropCompleted(success);
            e.consume();
        });

        dropZone.setOnMouseClicked(e -> {
            showMockIngredients(); // Simulated upload click
        });
    }

    private void showMockIngredients() {
        ingredientsBox.getChildren().clear();
        String[] items = {
            "Carrots", "Tomatoes", "Spinach", "Broccoli", "Onions",
            "Garlic", "Zucchini", "Peppers", "Cabbage", "Kale",
            "Celery", "Sweet Potatoes", "Mushrooms"
        };
        for (String item : items) {
            Label label = new Label("🟢 " + item);
            label.getStyleClass().add("ingredient-label");
            ingredientsBox.getChildren().add(label);
        }
    }

    // 🔁 Navigation actions
    @FXML private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML private void goToInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML private void handleLogout() throws IOException {
        App.setRoot("start");
    }
}
