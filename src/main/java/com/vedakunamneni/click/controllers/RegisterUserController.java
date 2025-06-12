package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class RegisterUserController {
    @FXML
    private TextField registerUsernameField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void createUser() {
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            feedbackLabel.setText("Please fill in all fields!");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }

        if (DatabaseHelper.userExists(username)) {
            feedbackLabel.setText("Username already exists! Please try a different one.");
            feedbackLabel.setTextFill(Color.RED);
            registerUsernameField.clear();
            registerPasswordField.clear();
            registerUsernameField.requestFocus();
            return;
        }

        if (DatabaseHelper.registerUser(username, password)) {
            feedbackLabel.setText("Account Created! Return to Login!");
            feedbackLabel.setTextFill(Color.GREEN);
        } else {
            feedbackLabel.setText("Registration failed. Please try again.");
            feedbackLabel.setTextFill(Color.RED);
            registerUsernameField.clear();
            registerPasswordField.clear();
            registerUsernameField.requestFocus();
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}
