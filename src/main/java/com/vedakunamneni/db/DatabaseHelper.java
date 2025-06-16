package com.vedakunamneni.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:ecopantry.db";

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            
            // First create the table if it doesn't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS users (email TEXT PRIMARY KEY, password TEXT)");
            
            // Create inventory table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, ingredient_name TEXT, quantity INTEGER DEFAULT 1, date_added DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(user_email) REFERENCES users(email))");
            
            // Check if security_question and security_answer columns exist, if not add them
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)");
            boolean hasSecurityQuestion = false;
            boolean hasSecurityAnswer = false;
            
            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("security_question".equals(columnName)) {
                    hasSecurityQuestion = true;
                } else if ("security_answer".equals(columnName)) {
                    hasSecurityAnswer = true;
                }
            }
            
            // Add missing columns
            if (!hasSecurityQuestion) {
                stmt.execute("ALTER TABLE users ADD COLUMN security_question TEXT");
                System.out.println("Added security_question column to users table");
            }
            if (!hasSecurityAnswer) {
                stmt.execute("ALTER TABLE users ADD COLUMN security_answer TEXT");
                System.out.println("Added security_answer column to users table");
            }
            
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean validateLogin(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // User found
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean userExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean registerUser(String email, String password, String securityQuestion, String securityAnswer) {
        if (userExists(email)) {
            System.out.println("User already exists: " + email);
            return false;
        }

        String sql = "INSERT INTO users (email, password, security_question, security_answer) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.setString(3, securityQuestion);
            pstmt.setString(4, securityAnswer.toLowerCase().trim()); // Store answer in lowercase for easier comparison
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Registration successful. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String getUserSecurityQuestion(String email) {
        String sql = "SELECT security_question FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("security_question");
            }
        } catch (SQLException e) {
            System.err.println("Error getting security question: " + e.getMessage());
        }
        return null;
    }

    public static boolean validateSecurityAnswer(String email, String answer) {
        String sql = "SELECT security_answer FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedAnswer = rs.getString("security_answer");
                return storedAnswer.equalsIgnoreCase(answer.trim());
            }
        } catch (SQLException e) {
            System.err.println("Error validating security answer: " + e.getMessage());
        }
        return false;
    }

    public static String getUserPassword(String email) {
        String sql = "SELECT password FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            System.err.println("Error getting user password: " + e.getMessage());
        }
        return null;
    }
    
    // Inventory management methods
    public static boolean addIngredientToInventory(String userEmail, String ingredientName, int quantity) {
        // Check if ingredient already exists, if so update quantity
        String checkSql = "SELECT quantity FROM inventory WHERE user_email = ? AND ingredient_name = ?";
        String updateSql = "UPDATE inventory SET quantity = quantity + ? WHERE user_email = ? AND ingredient_name = ?";
        String insertSql = "INSERT INTO inventory (user_email, ingredient_name, quantity) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Check if ingredient exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, userEmail);
                checkStmt.setString(2, ingredientName);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing ingredient
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, quantity);
                        updateStmt.setString(2, userEmail);
                        updateStmt.setString(3, ingredientName);
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    // Insert new ingredient
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, userEmail);
                        insertStmt.setString(2, ingredientName);
                        insertStmt.setInt(3, quantity);
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding ingredient to inventory: " + e.getMessage());
            return false;
        }
    }
    
    public static java.util.List<String> getUserInventory(String userEmail) {
        java.util.List<String> inventory = new java.util.ArrayList<>();
        String sql = "SELECT ingredient_name, quantity FROM inventory WHERE user_email = ? ORDER BY ingredient_name";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String ingredient = rs.getString("ingredient_name");
                int quantity = rs.getInt("quantity");
                inventory.add(ingredient + " (x" + quantity + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error getting user inventory: " + e.getMessage());
        }
        
        return inventory;
    }
    
    public static boolean removeIngredientFromInventory(String userEmail, String ingredientName) {
        String sql = "DELETE FROM inventory WHERE user_email = ? AND ingredient_name = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, ingredientName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing ingredient from inventory: " + e.getMessage());
            return false;
        }
    }
}
