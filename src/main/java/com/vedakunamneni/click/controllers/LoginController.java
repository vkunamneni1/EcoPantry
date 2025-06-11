package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (DatabaseHelper.validateLogin(email, password)) {
            App.setRoot("dashboard");
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void handleForgotPassword() throws IOException {
        App.setRoot("forgot_password");
    }

    @FXML
    private void registerUser() throws IOException {
        App.setRoot("register_user");
    }
}