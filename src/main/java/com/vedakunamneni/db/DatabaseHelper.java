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
            
            // Create statistics tracking table
            stmt.execute("CREATE TABLE IF NOT EXISTS food_statistics (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_email TEXT, " +
                        "ingredient_name TEXT, " +
                        "quantity INTEGER, " +
                        "status TEXT CHECK(status IN ('USED', 'WASTED')), " +
                        "date_tracked DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "days_until_expiration INTEGER, " +
                        "FOREIGN KEY(user_email) REFERENCES users(email))");
            
            // Create shopping list table
            stmt.execute("CREATE TABLE IF NOT EXISTS shopping_list (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_email TEXT, " +
                        "ingredient_name TEXT, " +
                        "recipe_name TEXT, " +
                        "is_purchased BOOLEAN DEFAULT FALSE, " +
                        "date_added DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_email) REFERENCES users(email))");
            
            // Create favorite recipes table
            stmt.execute("CREATE TABLE IF NOT EXISTS favorite_recipes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_email TEXT, " +
                        "recipe_name TEXT, " +
                        "recipe_data TEXT, " +
                        "date_favorited DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_email) REFERENCES users(email), " +
                        "UNIQUE(user_email, recipe_name))");
            
            
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
            
            // Check if points and level columns exist, if not add them
            ResultSet pointsRs = stmt.executeQuery("PRAGMA table_info(users)");
            boolean hasPoints = false;
            boolean hasLevel = false;
            
            while (pointsRs.next()) {
                String columnName = pointsRs.getString("name");
                if ("points".equals(columnName)) {
                    hasPoints = true;
                } else if ("level".equals(columnName)) {
                    hasLevel = true;
                }
            }
            
            if (!hasPoints) {
                stmt.execute("ALTER TABLE users ADD COLUMN points INTEGER DEFAULT 0");
                System.out.println("Added points column to users table");
            }
            
            if (!hasLevel) {
                stmt.execute("ALTER TABLE users ADD COLUMN level INTEGER DEFAULT 1");
                System.out.println("Added level column to users table");
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
            
            if (rowsAffected > 0) {
                // Award points for adding items: 5 points per item
                addPoints(userEmail, quantity * 5, "adding " + quantity + " " + ingredientName + " to inventory");
                System.out.println("Added " + ingredientName + " to inventory for " + userEmail);
                return true;
            }
            return false;
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
    
    // Statistics tracking methods
    public static boolean trackFoodStatistic(String userEmail, String ingredientName, int quantity, String status, int daysUntilExpiration) {
        System.out.println("=== TRACKING FOOD STATISTIC ===");
        System.out.println("User: " + userEmail);
        System.out.println("Ingredient: " + ingredientName);
        System.out.println("Quantity: " + quantity);
        System.out.println("Status: " + status);
        System.out.println("Days until expiration: " + daysUntilExpiration);
        
        String sql = "INSERT INTO food_statistics (user_email, ingredient_name, quantity, status, days_until_expiration) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, ingredientName);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, status);
            pstmt.setInt(5, daysUntilExpiration);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Award points based on food usage
                if ("USED".equals(status)) {
                    // Award more points for using food (10 points per item)
                    addPoints(userEmail, quantity * 10, "using " + quantity + " " + ingredientName);
                } else if ("WASTED".equals(status)) {
                    // Award fewer points for wasting food (2 points per item - still encouraging tracking)
                    addPoints(userEmail, quantity * 2, "tracking wasted " + quantity + " " + ingredientName);
                }
            }
            
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("=== END TRACKING ===");
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error tracking food statistic: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean removeFromInventoryWithTracking(int inventoryId, boolean wasUsed) {
        // Use a single connection for the entire transaction
        String getItemSql = "SELECT user_email, ingredient_name, quantity, expiration_date FROM inventory WHERE id = ?";
        String deleteSql = "DELETE FROM inventory WHERE id = ?";
        String trackSql = "INSERT INTO food_statistics (user_email, ingredient_name, quantity, status, days_until_expiration) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Get item details
                String userEmail = null;
                String ingredientName = null;
                int quantity = 0;
                int daysUntilExpiration = 0;
                
                try (PreparedStatement getStmt = conn.prepareStatement(getItemSql)) {
                    getStmt.setInt(1, inventoryId);
                    ResultSet rs = getStmt.executeQuery();
                    
                    if (rs.next()) {
                        userEmail = rs.getString("user_email");
                        ingredientName = rs.getString("ingredient_name");
                        quantity = rs.getInt("quantity");
                        String expirationDateStr = rs.getString("expiration_date");
                        
                        // Calculate days until expiration
                        if (expirationDateStr != null) {
                            try {
                                java.time.LocalDate expirationDate = java.time.LocalDate.parse(expirationDateStr);
                                daysUntilExpiration = (int) java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), expirationDate);
                            } catch (Exception e) {
                                daysUntilExpiration = 0;
                            }
                        }
                    } else {
                        conn.rollback();
                        return false; // Item not found
                    }
                }
                
                // Track the statistic first
                String status = wasUsed ? "USED" : "WASTED";
                System.out.println("=== TRACKING FOOD STATISTIC IN TRANSACTION ===");
                System.out.println("User: " + userEmail);
                System.out.println("Ingredient: " + ingredientName);
                System.out.println("Quantity: " + quantity);
                System.out.println("Status: " + status);
                System.out.println("Days until expiration: " + daysUntilExpiration);
                
                try (PreparedStatement trackStmt = conn.prepareStatement(trackSql)) {
                    trackStmt.setString(1, userEmail);
                    trackStmt.setString(2, ingredientName);
                    trackStmt.setInt(3, quantity);
                    trackStmt.setString(4, status);
                    trackStmt.setInt(5, daysUntilExpiration);
                    int trackRows = trackStmt.executeUpdate();
                    System.out.println("Statistics tracking result: " + trackRows + " rows affected");
                }
                
                // Remove from inventory
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, inventoryId);
                    int deleteRows = deleteStmt.executeUpdate();
                    System.out.println("Inventory deletion result: " + deleteRows + " rows affected");
                    
                    if (deleteRows > 0) {
                        // Commit transaction
                        conn.commit();
                        System.out.println("Transaction committed successfully!");
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Error in transaction, rolling back: " + e.getMessage());
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error removing from inventory with tracking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static java.util.Map<String, Integer> getFoodStatistics(String userEmail) {
        System.out.println("=== GETTING FOOD STATISTICS ===");
        System.out.println("User: " + userEmail);
        
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        String sql = "SELECT status, SUM(quantity) as total FROM food_statistics WHERE user_email = ? GROUP BY status";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("Query results:");
            while (rs.next()) {
                String status = rs.getString("status");
                int total = rs.getInt("total");
                stats.put(status, total);
                System.out.println("Status: " + status + ", Total: " + total);
            }
            
            System.out.println("Final stats map: " + stats);
            System.out.println("=== END GETTING STATISTICS ===");
        } catch (SQLException e) {
            System.err.println("Error getting food statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    public static boolean clearFoodStatistics(String userEmail) {
        String sql = "DELETE FROM food_statistics WHERE user_email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected >= 0; // Return true even if no rows were deleted
        } catch (SQLException e) {
            System.err.println("Error clearing food statistics: " + e.getMessage());
            return false;
        }
    }
    
    public static java.util.Map<String, Object> getDetailedStatistics(String userEmail) {
        java.util.Map<String, Object> detailedStats = new java.util.HashMap<>();
        
        // Get items added this week from current inventory
        String addedThisWeekSql = "SELECT COUNT(*) as count FROM inventory WHERE user_email = ? AND date_added >= date('now', '-7 days')";
        
        // Get most wasted category
        String mostWastedSql = "SELECT ingredient_name, SUM(quantity) as total FROM food_statistics WHERE user_email = ? AND status = 'WASTED' GROUP BY ingredient_name ORDER BY total DESC LIMIT 1";
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Added this week
            try (PreparedStatement pstmt = conn.prepareStatement(addedThisWeekSql)) {
                pstmt.setString(1, userEmail);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    detailedStats.put("addedThisWeek", rs.getInt("count"));
                }
            }
            
            // Most wasted category
            try (PreparedStatement pstmt = conn.prepareStatement(mostWastedSql)) {
                pstmt.setString(1, userEmail);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    detailedStats.put("mostWastedCategory", rs.getString("ingredient_name"));
                } else {
                    detailedStats.put("mostWastedCategory", "N/A");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting detailed statistics: " + e.getMessage());
        }
        
        return detailedStats;
    }

    public static boolean updateUserPassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            return false;
        }
    }

    // Debug method to show all food statistics in the database
    public static void debugFoodStatistics(String userEmail) {
        System.out.println("=== DEBUG: ALL FOOD STATISTICS FOR USER " + userEmail + " ===");
        String sql = "SELECT * FROM food_statistics WHERE user_email = ? ORDER BY date_tracked DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("Record " + count + ":");
                System.out.println("  ID: " + rs.getInt("id"));
                System.out.println("  Ingredient: " + rs.getString("ingredient_name"));
                System.out.println("  Quantity: " + rs.getInt("quantity"));
                System.out.println("  Status: " + rs.getString("status"));
                System.out.println("  Date: " + rs.getString("date_tracked"));
                System.out.println("  Days until expiration: " + rs.getInt("days_until_expiration"));
                System.out.println();
            }
            
            if (count == 0) {
                System.out.println("NO RECORDS FOUND!");
            } else {
                System.out.println("Total records: " + count);
            }
        } catch (SQLException e) {
            System.err.println("Error in debug: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== END DEBUG ===");
    }
    
    // Level and Points System Methods
    public static void addPoints(String userEmail, int points, String reason) {
        String selectSql = "SELECT points, level FROM users WHERE email = ?";
        String updateSql = "UPDATE users SET points = ?, level = ? WHERE email = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            
            // Get current points and level
            selectStmt.setString(1, userEmail);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                int currentPoints = rs.getInt("points");
                int currentLevel = rs.getInt("level");
                int newPoints = currentPoints + points;
                int newLevel = calculateLevel(newPoints);
                
                boolean leveledUp = newLevel > currentLevel;
                
                // Update points and level
                updateStmt.setInt(1, newPoints);
                updateStmt.setInt(2, newLevel);
                updateStmt.setString(3, userEmail);
                updateStmt.executeUpdate();
                
                System.out.println("Points added: +" + points + " for " + reason);
                System.out.println("Total points: " + newPoints + " | Level: " + newLevel);
                
                if (leveledUp) {
                    System.out.println("🎉 LEVEL UP! You reached level " + newLevel + "!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding points: " + e.getMessage());
        }
    }
    
    public static int calculateLevel(int points) {
        // Level system: Each level requires more points than the previous
        // Level 1: 0-99 points
        // Level 2: 100-299 points  
        // Level 3: 300-599 points
        // Level 4: 600-999 points
        // Level 5: 1000-1499 points
        // And so on...
        if (points < 100) return 1;
        if (points < 300) return 2;
        if (points < 600) return 3;
        if (points < 1000) return 4;
        if (points < 1500) return 5;
        if (points < 2100) return 6;
        if (points < 2800) return 7;
        if (points < 3600) return 8;
        if (points < 4500) return 9;
        if (points < 5500) return 10;
        return 10 + (points - 5500) / 1000; // Level 11+ needs 1000 points each
    }
    
    public static int getPointsForNextLevel(int currentPoints) {
        int currentLevel = calculateLevel(currentPoints);
        int nextLevel = currentLevel + 1;
        
        // Calculate points needed for next level
        if (nextLevel == 2) return 100 - currentPoints;
        if (nextLevel == 3) return 300 - currentPoints;
        if (nextLevel == 4) return 600 - currentPoints;
        if (nextLevel == 5) return 1000 - currentPoints;
        if (nextLevel == 6) return 1500 - currentPoints;
        if (nextLevel == 7) return 2100 - currentPoints;
        if (nextLevel == 8) return 2800 - currentPoints;
        if (nextLevel == 9) return 3600 - currentPoints;
        if (nextLevel == 10) return 4500 - currentPoints;
        if (nextLevel == 11) return 5500 - currentPoints;
        
        // For level 11+
        int nextLevelPoints = 5500 + (nextLevel - 11) * 1000;
        return nextLevelPoints - currentPoints;
    }
    
    public static int[] getUserPointsAndLevel(String userEmail) {
        String sql = "SELECT points, level FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new int[]{rs.getInt("points"), rs.getInt("level")};
            }
        } catch (SQLException e) {
            System.err.println("Error getting user points and level: " + e.getMessage());
        }
        return new int[]{0, 1}; // Default values
    }

    public static boolean clearAllInventory(String userEmail) {
        String sql = "DELETE FROM inventory WHERE user_email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Cleared " + rowsAffected + " items from inventory for " + userEmail);
                // Award small points for clearing inventory (1 point per item cleared)
                addPoints(userEmail, rowsAffected, "clearing " + rowsAffected + " items from inventory");
            }
            
            return rowsAffected >= 0; // Return true even if no items were deleted
        } catch (SQLException e) {
            System.err.println("Error clearing inventory: " + e.getMessage());
            return false;
        }
    }
    
    // Shopping List methods
    public static boolean addToShoppingList(String userEmail, String ingredientName, String recipeName) {
        String sql = "INSERT INTO shopping_list (user_email, ingredient_name, recipe_name) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, ingredientName);
            pstmt.setString(3, recipeName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to shopping list: " + e.getMessage());
            return false;
        }
    }
    
    public static java.util.List<String> getShoppingList(String userEmail) {
        java.util.List<String> shoppingList = new java.util.ArrayList<>();
        String sql = "SELECT ingredient_name, recipe_name FROM shopping_list WHERE user_email = ? AND is_purchased = FALSE ORDER BY date_added ASC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String ingredient = rs.getString("ingredient_name");
                String recipe = rs.getString("recipe_name");
                shoppingList.add(ingredient + " (for " + recipe + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error getting shopping list: " + e.getMessage());
        }
        
        return shoppingList;
    }
    
    // Favorite Recipes methods
    public static boolean addToFavorites(String userEmail, String recipeName, String recipeData) {
        String sql = "INSERT OR REPLACE INTO favorite_recipes (user_email, recipe_name, recipe_data) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, recipeName);
            pstmt.setString(3, recipeData);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to favorites: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean isRecipeFavorited(String userEmail, String recipeName) {
        String sql = "SELECT COUNT(*) FROM favorite_recipes WHERE user_email = ? AND recipe_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, recipeName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking if recipe is favorited: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean removeFromFavorites(String userEmail, String recipeName) {
        String sql = "DELETE FROM favorite_recipes WHERE user_email = ? AND recipe_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            pstmt.setString(2, recipeName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing from favorites: " + e.getMessage());
            return false;
        }
    }
}
