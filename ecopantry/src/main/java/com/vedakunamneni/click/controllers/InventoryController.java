package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;

import javafx.fxml.FXML;

public class InventoryController {

    @FXML
    private void handleLogout() throws IOException {
        App.setRoot("start");
    }

    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }
}
