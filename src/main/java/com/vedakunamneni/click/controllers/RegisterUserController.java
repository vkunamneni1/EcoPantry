package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.security.SecureRandom;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
    private TextField registerPasswordVisibleField;

    @FXML
    private ComboBox<String> securityQuestionComboBox;

    @FXML
    private TextField securityAnswerField;

    @FXML
    private Label feedbackLabel;
    
    @FXML
    private Button generatePasswordButton;
    
    @FXML
    private Button togglePasswordButton;
    
    private boolean isPasswordVisible = false;

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
        
        // Sync password fields when user types manually
        registerPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isPasswordVisible) {
                registerPasswordVisibleField.setText(newValue);
                // Hide toggle button when user is typing their own password
                if (!newValue.isEmpty()) {
                    togglePasswordButton.setVisible(false);
                    togglePasswordButton.setManaged(false);
                }
            }
        });
        
        registerPasswordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isPasswordVisible) {
                registerPasswordField.setText(newValue);
            }
        });
    }

    @FXML
    private void createUser() {
        String username = registerUsernameField.getText().trim();
        String password = getCurrentPassword().trim(); // Use the current password from either field
        String securityQuestion = securityQuestionComboBox.getValue();
        String securityAnswer = securityAnswerField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || securityQuestion == null || securityAnswer.isEmpty()) {
            feedbackLabel.setText("Please fill in all fields!");
            feedbackLabel.setTextFill(Color.RED);
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            feedbackLabel.setText("Password must be at least 6 characters long!");
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
    private void generatePassword() {
        String generatedPassword = generateSecurePassword(12);
        
        // Show the generated password in the visible field
        registerPasswordVisibleField.setText(generatedPassword);
        registerPasswordField.setText(generatedPassword);
        
        // Switch to visible field to show the generated password
        showVisiblePassword();
        
        feedbackLabel.setText("✅ Strong password generated! (Length: " + generatedPassword.length() + ")");
        feedbackLabel.setTextFill(Color.GREEN);
    }
    
    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            showSecurePassword();
        } else {
            showVisiblePassword();
        }
    }
    
    private void showVisiblePassword() {
        String currentPassword = getCurrentPassword();
        registerPasswordVisibleField.setText(currentPassword);
        registerPasswordField.setVisible(false);
        registerPasswordField.setManaged(false);
        registerPasswordVisibleField.setVisible(true);
        registerPasswordVisibleField.setManaged(true);
        togglePasswordButton.setVisible(true);
        togglePasswordButton.setManaged(true);
        togglePasswordButton.setText("🙈"); // Hide password icon
        isPasswordVisible = true;
    }
    
    private void showSecurePassword() {
        String currentPassword = getCurrentPassword();
        registerPasswordField.setText(currentPassword);
        registerPasswordVisibleField.setVisible(false);
        registerPasswordVisibleField.setManaged(false);
        registerPasswordField.setVisible(true);
        registerPasswordField.setManaged(true);
        togglePasswordButton.setText("👁"); // Show password icon
        isPasswordVisible = false;
    }
    
    private String getCurrentPassword() {
        return isPasswordVisible ? registerPasswordVisibleField.getText() : registerPasswordField.getText();
    }
    
    private String generateSecurePassword(int length) {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        
        String allChars = upperCase + lowerCase + numbers + specialChars;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }
    
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        SecureRandom random = new SecureRandom();
        
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        
        return new String(characters);
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("login");
    }

}