package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterUserController {
    @FXML
    private TextField registerUsernameField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void createUser() throws IOException {
        String username = registerUsernameField.getText();
        String password = registerPasswordField.getText().trim();

        DatabaseHelper.registerUser(username, password);

        feedbackLabel.setText("Account Created! Return to Login!");
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}
