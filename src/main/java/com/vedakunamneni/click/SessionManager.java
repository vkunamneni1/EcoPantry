package com.vedakunamneni.click;

public class SessionManager {
    private static String currentUser = null;
    
    public static void setCurrentUser(String username) {
        currentUser = username;
    }
    
    public static String getCurrentUser() {
        return currentUser;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static void logout() {
        currentUser = null;
    }
    
    public static String getDisplayName() {
        if (currentUser == null) {
            return "Guest";
        }
        
        // Extract the part before @ if it's an email
        if (currentUser.contains("@")) {
            String username = currentUser.substring(0, currentUser.indexOf("@"));
            // Capitalize first letter
            return username.substring(0, 1).toUpperCase() + username.substring(1);
        }
        
        // Capitalize first letter of username
        return currentUser.substring(0, 1).toUpperCase() + currentUser.substring(1);
    }
    
    public static void clearSession() {
        currentUser = null;
    }
}
