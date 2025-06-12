package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordFieldVisible;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Keep the password fields in sync
        passwordField.textProperty().bindBidirectional(passwordFieldVisible.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            passwordFieldVisible.setManaged(true);
            passwordFieldVisible.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            passwordFieldVisible.requestFocus();
        } else {
            passwordFieldVisible.setManaged(false);
            passwordFieldVisible.setVisible(false);
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            passwordField.requestFocus();
        }
    }

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