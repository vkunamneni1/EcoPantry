package com.vedakunamneni.click.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Ingredient {
    private int id;
    private String name;
    private int quantity;
    private LocalDate expirationDate;
    private LocalDate dateAdded;
    
    public Ingredient(int id, String name, int quantity, LocalDate expirationDate, LocalDate dateAdded) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.dateAdded = dateAdded;
    }
    
    public Ingredient(String name, int quantity, LocalDate expirationDate) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.dateAdded = LocalDate.now();
    }
    
    public Ingredient(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = LocalDate.now().plusDays(7); // Default 7 days if not specified
        this.dateAdded = LocalDate.now();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public LocalDate getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    public LocalDate getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public long getDaysUntilExpiration() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }
    
    public ExpirationStatus getExpirationStatus() {
        long daysUntilExpiration = getDaysUntilExpiration();
        
        if (daysUntilExpiration < 0) {
            return ExpirationStatus.EXPIRED;
        } else if (daysUntilExpiration <= 2) {
            return ExpirationStatus.EXPIRING_SOON;
        } else if (daysUntilExpiration <= 7) {
            return ExpirationStatus.EXPIRING_THIS_WEEK;
        } else {
            return ExpirationStatus.FRESH;
        }
    }
    
    public enum ExpirationStatus {
        EXPIRED,        // Red - Past expiration
        EXPIRING_SOON,  // Red - 0-2 days
        EXPIRING_THIS_WEEK, // Yellow - 3-7 days
        FRESH           // Green - 8+ days
    }
    
    @Override
    public String toString() {
        return name + " (Qty: " + quantity + ", Expires: " + expirationDate + ")";
    }
}
