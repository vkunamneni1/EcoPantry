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
}
