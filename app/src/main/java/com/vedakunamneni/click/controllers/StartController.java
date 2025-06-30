package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;

import javafx.fxml.FXML;

public class StartController {

    @FXML
    private void goToLogin() throws IOException {
        App.setRoot("login");
    }
}
