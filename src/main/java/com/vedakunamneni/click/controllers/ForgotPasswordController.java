package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ForgotPasswordController {

    @FXML
    private TextField securityAnswerField;

    @FXML
    private TextField forgotUsernameField;

    @FXML
    private Label feedbackLabel;

    @FXML
    private void handleAnswerSubmit() throws IOException {
        String answer = securityAnswerField.getText();
        String forUser = forgotUsernameField.getText();

        if ("username".equalsIgnoreCase(forUser.trim())) {
            if ("answer".equalsIgnoreCase(answer.trim())) {
                feedbackLabel.setText("✅ Correct! Your password is: password");
            } else {
                feedbackLabel.setText("❌ Incorrect answer. Try again.");
            }
        } else {
            feedbackLabel.setText("❌ Incorrect answer. Try again.");
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}
