package com.vedakunamneni.click.controllers;

import java.io.IOException;

import com.vedakunamneni.click.App;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button resetPasswordButton;

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
            // Show password reset fields
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            resetPasswordButton.setVisible(true);
            resetPasswordButton.setManaged(true);
            
            // Hide previous fields
            securityAnswerField.setVisible(false);
            securityAnswerField.setManaged(false);
            submitAnswerButton.setVisible(false);
            submitAnswerButton.setManaged(false);
            
            feedbackLabel.setText("✅ Correct! Please enter your new password.");
            feedbackLabel.setTextFill(Color.GREEN);
        } else {
            feedbackLabel.setText("❌ Incorrect answer. Please try again.");
            feedbackLabel.setTextFill(Color.RED);
            securityAnswerField.clear();
        }
    }
    
    @FXML
    private void handlePasswordReset() throws IOException {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String username = forgotUsernameField.getText().trim();
        
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            feedbackLabel.setText("Please fill in both password fields.");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }
        
        if (newPassword.length() < 6) {
            feedbackLabel.setText("Password must be at least 6 characters long.");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            feedbackLabel.setText("Passwords do not match.");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }
        
        // Update the password in the database
        if (DatabaseHelper.updateUserPassword(username, newPassword)) {
            feedbackLabel.setText("✅ Password updated successfully! You can now log in.");
            feedbackLabel.setTextFill(Color.GREEN);
            
            // Hide password fields
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            resetPasswordButton.setVisible(false);
            resetPasswordButton.setManaged(false);
        } else {
            feedbackLabel.setText("❌ Failed to update password. Please try again.");
            feedbackLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}
