package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterUserController implements Initializable {
    @FXML
    private TextField registerUsernameField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private ComboBox<String> securityQuestionComboBox;

    @FXML
    private TextField securityAnswerField;

    @FXML
    private Label feedbackLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate security questions
        securityQuestionComboBox.getItems().addAll(
            "What is your favorite food?",
            "What was the name of your first pet?",
            "In what city were you born?",
            "What is your mother's maiden name?",
            "What was your first car?",
            "What elementary school did you attend?"
        );
    }

    @FXML
    private void createUser() {
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText().trim();
        String securityQuestion = securityQuestionComboBox.getValue();
        String securityAnswer = securityAnswerField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || securityQuestion == null || securityAnswer.isEmpty()) {
            feedbackLabel.setText("Please fill in all fields!");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }

        if (DatabaseHelper.userExists(username)) {
            feedbackLabel.setText("Username already exists! Please try a different one.");
            feedbackLabel.setTextFill(Color.RED);
            registerUsernameField.clear();
            registerPasswordField.clear();
            securityAnswerField.clear();
            securityQuestionComboBox.setValue(null);
            registerUsernameField.requestFocus();
            return;
        }

        if (DatabaseHelper.registerUser(username, password, securityQuestion, securityAnswer)) {
            feedbackLabel.setText("Account Created! Return to Login!");
            feedbackLabel.setTextFill(Color.GREEN);
        } else {
            feedbackLabel.setText("Registration failed. Please try again.");
            feedbackLabel.setTextFill(Color.RED);
            registerUsernameField.clear();
            registerPasswordField.clear();
            securityAnswerField.clear();
            securityQuestionComboBox.setValue(null);
            registerUsernameField.requestFocus();
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}