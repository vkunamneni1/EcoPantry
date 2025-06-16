package com.vedakunamneni.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.vedakunamneni.click.models.Ingredient;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:ecopantry.db";

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            
            // First create the table if it doesn't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS users (email TEXT PRIMARY KEY, password TEXT)");
            
            // Create inventory table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, ingredient_name TEXT, quantity INTEGER DEFAULT 1, expiration_date DATE, date_added DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(user_email) REFERENCES users(email))");
            
            // Check if expiration_date column exists in inventory table, if not add it
            ResultSet inventoryRs = stmt.executeQuery("PRAGMA table_info(inventory)");
            boolean hasExpirationDate = false;
            
            while (inventoryRs.next()) {
                String columnName = inventoryRs.getString("name");
                if ("expiration_date".equals(columnName)) {
                    hasExpirationDate = true;
                    break;
                }
            }
            
            if (!hasExpirationDate) {
                stmt.execute("ALTER TABLE inventory ADD COLUMN expiration_date DATE");
                System.out.println("Added expiration_date column to inventory table");
            }
            
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
    public static boolean addToInventory(String userEmail, String ingredientName, int quantity, java.time.LocalDate expirationDate) {
        String sql = "INSERT INTO inventory (user_email, ingredient_name, quantity, expiration_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, ingredientName);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, expirationDate.toString());
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Added " + ingredientName + " to inventory for " + userEmail);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to inventory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static java.util.List<Ingredient> getUserInventory(String userEmail) {
        java.util.List<Ingredient> inventory = new java.util.ArrayList<>();
        String sql = "SELECT id, ingredient_name, quantity, expiration_date, date_added FROM inventory WHERE user_email = ? ORDER BY expiration_date ASC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("ingredient_name");
                int quantity = rs.getInt("quantity");
                String expirationDateStr = rs.getString("expiration_date");
                String dateAddedStr = rs.getString("date_added");
                
                java.time.LocalDate expirationDate = null;
                java.time.LocalDate dateAdded = null;
                
                try {
                    if (expirationDateStr != null) {
                        expirationDate = java.time.LocalDate.parse(expirationDateStr);
                    } else {
                        expirationDate = java.time.LocalDate.now().plusDays(7); // Default 7 days
                    }
                    
                    if (dateAddedStr != null) {
                        // Handle both date and datetime formats
                        if (dateAddedStr.contains(" ")) {
                            dateAdded = java.time.LocalDateTime.parse(dateAddedStr.replace(" ", "T")).toLocalDate();
                        } else {
                            dateAdded = java.time.LocalDate.parse(dateAddedStr);
                        }
                    } else {
                        dateAdded = java.time.LocalDate.now();
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + e.getMessage());
                    expirationDate = java.time.LocalDate.now().plusDays(7);
                    dateAdded = java.time.LocalDate.now();
                }
                
                inventory.add(new com.vedakunamneni.click.models.Ingredient(id, name, quantity, expirationDate, dateAdded));
            }
        } catch (SQLException e) {
            System.err.println("Error getting user inventory: " + e.getMessage());
            e.printStackTrace();
        }
        
        return inventory;
    }
    
    public static boolean removeFromInventory(int inventoryId) {
        String sql = "DELETE FROM inventory WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing from inventory: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean updateInventoryQuantity(int inventoryId, int newQuantity) {
        String sql = "UPDATE inventory SET quantity = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, inventoryId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating inventory quantity: " + e.getMessage());
            return false;
        }
    }
    
    // Statistics-related methods
    public static java.util.List<com.vedakunamneni.click.models.Ingredient> getInventoryItems(int userId) {
        // First get user email from userId
        String userEmail = getUserEmailFromId(userId);
        if (userEmail == null) {
            return new java.util.ArrayList<>();
        }
        return getUserInventory(userEmail);
    }

    public static String getUserEmailFromId(int userId) {
        // For now, we'll use a simple mapping since we don't have user IDs in our schema
        // In a real app, you'd have a users table with ID as primary key
        String sql = "SELECT email FROM users LIMIT 1 OFFSET ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId - 1); // Assuming userId starts from 1
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Error getting user email: " + e.getMessage());
        }
        return null;
    }

    public static int getWastedItemsCount(int userId) {
        // This would track items that expired without being used
        // For now, we'll simulate this data
        String userEmail = getUserEmailFromId(userId);
        if (userEmail == null) return 0;

        // In a real app, you'd have a waste_log table or status field
        // For now, simulate based on expired items that might have been removed
        return getRandomBetween(0, 3);
    }

    public static int getSavedItemsCount(int userId) {
        // This would track items that were used before expiring
        // For now, we'll simulate this data
        String userEmail = getUserEmailFromId(userId);
        if (userEmail == null) return 0;

        // In a real app, you'd have a usage_log table
        // For now, simulate based on inventory activity
        java.util.List<com.vedakunamneni.click.models.Ingredient> items = getInventoryItems(userId);
        return Math.max(0, items.size() * 2 + getRandomBetween(5, 15));
    }

    private static int getRandomBetween(int min, int max) {
        return min + (int)(Math.random() * (max - min + 1));
    }

    // Method to add item to waste log (for future use)
    public static boolean addToWasteLog(String userEmail, String ingredientName, java.time.LocalDate expirationDate, String reason) {
        // Create waste_log table if it doesn't exist
        String createTableSql = "CREATE TABLE IF NOT EXISTS waste_log (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, ingredient_name TEXT, expiration_date DATE, waste_date DATE DEFAULT CURRENT_DATE, reason TEXT, FOREIGN KEY(user_email) REFERENCES users(email))";
        
        String sql = "INSERT INTO waste_log (user_email, ingredient_name, expiration_date, reason) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Create table if needed
            stmt.execute(createTableSql);
            
            pstmt.setString(1, userEmail);
            pstmt.setString(2, ingredientName);
            pstmt.setString(3, expirationDate.toString());
            pstmt.setString(4, reason);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to waste log: " + e.getMessage());
            return false;
        }
    }

    // Method to add item to usage log (for future use)
    public static boolean addToUsageLog(String userEmail, String ingredientName, java.time.LocalDate expirationDate) {
        // Create usage_log table if it doesn't exist
        String createTableSql = "CREATE TABLE IF NOT EXISTS usage_log (id INTEGER PRIMARY KEY AUTOINCREMENT, user_email TEXT, ingredient_name TEXT, expiration_date DATE, usage_date DATE DEFAULT CURRENT_DATE, FOREIGN KEY(user_email) REFERENCES users(email))";
        
        String sql = "INSERT INTO usage_log (user_email, ingredient_name, expiration_date) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Create table if needed
            stmt.execute(createTableSql);
            
            pstmt.setString(1, userEmail);
            pstmt.setString(2, ingredientName);
            pstmt.setString(3, expirationDate.toString());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to usage log: " + e.getMessage());
            return false;
        }
    }
}
