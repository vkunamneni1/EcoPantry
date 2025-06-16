package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class ForgotPasswordController {

    @FXML
    private TextField securityAnswerField;

    @FXML
    private TextField forgotUsernameField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Label securityQuestionLabel;

    @FXML
    private Button submitAnswerButton;

    @FXML
    private void getSecurityQuestion() {
        String username = forgotUsernameField.getText().trim();
        
        if (username.isEmpty()) {
            feedbackLabel.setText("Please enter your username.");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }

        if (!DatabaseHelper.userExists(username)) {
            feedbackLabel.setText("Username not found.");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }

        String securityQuestion = DatabaseHelper.getUserSecurityQuestion(username);
        if (securityQuestion != null) {
            securityQuestionLabel.setText(securityQuestion);
            securityQuestionLabel.setVisible(true);
            securityQuestionLabel.setManaged(true);
            securityAnswerField.setVisible(true);
            securityAnswerField.setManaged(true);
            submitAnswerButton.setVisible(true);
            submitAnswerButton.setManaged(true);
            feedbackLabel.setText("Please answer your security question.");
            feedbackLabel.setTextFill(Color.BLUE);
        } else {
            feedbackLabel.setText("Unable to retrieve security question.");
            feedbackLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleAnswerSubmit() throws IOException {
        String answer = securityAnswerField.getText().trim();
        String username = forgotUsernameField.getText().trim();

        if (answer.isEmpty()) {
            feedbackLabel.setText("Please enter your answer.");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }

        if (DatabaseHelper.validateSecurityAnswer(username, answer)) {
            String password = DatabaseHelper.getUserPassword(username);
            feedbackLabel.setText("✅ Correct! Your password is: " + password);
            feedbackLabel.setTextFill(Color.GREEN);
        } else {
            feedbackLabel.setText("❌ Incorrect answer. Please try again.");
            feedbackLabel.setTextFill(Color.RED);
            securityAnswerField.clear();
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}
