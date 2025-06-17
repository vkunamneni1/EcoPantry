package com.vedakunamneni.click.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ScannerController {

    @FXML
    private VBox dropZone;

    @FXML
    private VBox ingredientsBox;

    @FXML
    private VBox ingredientsSection;

    @FXML
    private Button addSelectedButton;

    private List<CheckBox> ingredientCheckBoxes = new ArrayList<>();
    private Map<CheckBox, DatePicker> ingredientDatePickers = new HashMap<>();
    private Map<CheckBox, TextField> ingredientQuantityFields = new HashMap<>();
    private File uploadedReceiptFile;

    // Common food items that might appear on receipts
    private static final List<String> COMMON_FOOD_ITEMS = Arrays.asList(
        // Fruits
        "apple", "apples", "banana", "bananas", "orange", "oranges", "grape", "grapes", 
        "strawberry", "strawberries", "blueberry", "blueberries", "lemon", "lemons", 
        "lime", "limes", "avocado", "avocados", "mango", "mangos", "pineapple", 
        "watermelon", "cantaloupe", "peach", "peaches", "pear", "pears",
        
        // Vegetables  
        "tomato", "tomatoes", "potato", "potatoes", "onion", "onions", "carrot", "carrots",
        "broccoli", "spinach", "lettuce", "cucumber", "cucumbers", "bell pepper", "peppers",
        "celery", "mushroom", "mushrooms", "zucchini", "squash", "cabbage", "kale",
        "garlic", "ginger", "cilantro", "parsley", "basil", "green beans", "corn",
        
        // Proteins
        "chicken", "beef", "pork", "fish", "salmon", "turkey", "ham", "bacon", "sausage",
        "ground beef", "chicken breast", "pork chops", "steak", "eggs", "tofu",
        
        // Dairy
        "milk", "cheese", "yogurt", "butter", "cream", "sour cream", "cottage cheese",
        "cheddar", "mozzarella", "parmesan", "swiss cheese",
        
        // Pantry items
        "bread", "rice", "pasta", "flour", "sugar", "salt", "pepper", "oil", "olive oil",
        "vinegar", "honey", "oats", "cereal", "crackers", "beans", "lentils", "quinoa",
        
        // Beverages & Others
        "juice", "coffee", "tea", "soup", "sauce", "salsa", "nuts", "almonds", "peanuts"
    );

    @FXML
    public void initialize() {
        setupDropZone();
    }

    private void setupDropZone() {
        dropZone.setOnDragOver(e -> {
            if (e.getGestureSource() != dropZone && e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        dropZone.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (isImageFile(file)) {
                    success = true;
                    uploadedReceiptFile = file;
                    processReceipt(file);
                } else {
                    showAlert("Invalid File", "Please upload an image file (JPG, PNG, PDF)");
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });

        dropZone.setOnMouseClicked(e -> {
            openFileChooser();
        });
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Receipt Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dropZone.getScene().getWindow());
        if (selectedFile != null) {
            uploadedReceiptFile = selectedFile;
            processReceipt(selectedFile);
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".pdf");
    }

    private void processReceipt(File receiptFile) {
        // Show processing message
        dropZone.getChildren().clear();
        Label processingLabel = new Label("🔍 Processing receipt...");
        processingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2196F3;");
        dropZone.getChildren().add(processingLabel);
        
        // Simulate processing delay and then extract ingredients
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate processing time
                
                // Extract text from receipt (simplified simulation)
                String receiptText = simulateOCR(receiptFile);
                
                // Extract ingredients from text
                List<String> detectedIngredients = extractIngredientsFromText(receiptText);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    showDetectedIngredients(detectedIngredients);
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private String simulateOCR(File receiptFile) {
        // In a real implementation, this would use an OCR service like Tesseract, 
        // Google Cloud Vision API, or AWS Textract
        // For now, we'll simulate realistic receipt text
        
        String fileName = receiptFile.getName().toLowerCase();
        
        // Return different mock receipt texts based on filename or random selection
        String[] mockReceipts = {
            "WHOLE FOODS MARKET\n" +
            "ORGANIC BANANAS         $3.49\n" +
            "ROMA TOMATOES          $2.99\n" +
            "FRESH SPINACH          $4.29\n" +
            "CHICKEN BREAST         $8.99\n" +
            "WHOLE MILK             $3.79\n" +
            "CHEDDAR CHEESE         $5.49\n" +
            "BREAD WHOLE WHEAT      $2.99\n" +
            "OLIVE OIL EXTRA        $7.99\n" +
            "GARLIC BULBS           $1.49\n" +
            "BROCCOLI CROWNS        $3.99",
            
            "SAFEWAY STORE #1234\n" +
            "GROUND BEEF 85/15      $6.99\n" +
            "YELLOW ONIONS          $1.99\n" +
            "BELL PEPPERS           $3.49\n" +
            "MUSHROOMS WHITE        $2.79\n" +
            "PASTA PENNE            $1.99\n" +
            "MARINARA SAUCE         $2.49\n" +
            "PARMESAN CHEESE        $4.99\n" +
            "LETTUCE ROMAINE        $2.99\n" +
            "CUCUMBERS              $1.79\n" +
            "EGGS LARGE DOZEN       $3.29",
            
            "TRADER JOES\n" +
            "AVOCADOS HASS          $3.99\n" +
            "KALE ORGANIC           $2.49\n" +
            "SWEET POTATOES         $2.99\n" +
            "SALMON FILLET          $12.99\n" +
            "QUINOA TRICOLOR        $4.49\n" +
            "ALMOND BUTTER          $7.99\n" +
            "COCONUT MILK           $1.99\n" +
            "BLUEBERRIES FRESH      $4.99\n" +
            "CARROTS ORGANIC        $1.99\n" +
            "GREEK YOGURT           $5.99"
        };
        
        // Return a random receipt or based on filename
        int index = Math.abs(fileName.hashCode()) % mockReceipts.length;
        return mockReceipts[index];
    }

    private List<String> extractIngredientsFromText(String receiptText) {
        List<String> detectedIngredients = new ArrayList<>();
        String[] lines = receiptText.toLowerCase().split("\n");
        
        for (String line : lines) {
            // Remove price patterns
            line = line.replaceAll("\\$[0-9]+\\.[0-9]{2}", "").trim();
            
            // Check each line against our food items list
            for (String foodItem : COMMON_FOOD_ITEMS) {
                if (line.contains(foodItem) && !detectedIngredients.contains(formatIngredientName(foodItem))) {
                    detectedIngredients.add(formatIngredientName(foodItem));
                    break; // Only match one item per line
                }
            }
            
            // Also check for common patterns
            if (line.contains("organic") || line.contains("fresh") || line.contains("whole")) {
                // Try to extract the actual ingredient name
                String cleanedLine = line.replaceAll("(organic|fresh|whole|lb|lbs|each|bag|bunch)", "").trim();
                if (cleanedLine.length() > 2 && !cleanedLine.matches(".*\\d.*") && !detectedIngredients.contains(cleanedLine)) {
                    for (String foodItem : COMMON_FOOD_ITEMS) {
                        if (cleanedLine.contains(foodItem)) {
                            detectedIngredients.add(formatIngredientName(foodItem));
                            break;
                        }
                    }
                }
            }
        }
        
        return detectedIngredients;
    }

    private String formatIngredientName(String ingredient) {
        // Capitalize first letter and handle plurals
        String formatted = ingredient.substring(0, 1).toUpperCase() + ingredient.substring(1);
        
        // Convert plurals to singular for consistency
        if (formatted.endsWith("ies")) {
            formatted = formatted.substring(0, formatted.length() - 3) + "y";
        } else if (formatted.endsWith("s") && !formatted.endsWith("ss")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        
        return formatted;
    }

    private void showDetectedIngredients(List<String> detectedIngredients) {
        ingredientsBox.getChildren().clear();
        ingredientCheckBoxes.clear();
        ingredientDatePickers.clear();
        ingredientQuantityFields.clear();
        
        // If no ingredients detected, show message
        if (detectedIngredients.isEmpty()) {
            Label noItemsLabel = new Label("⚠️ No food items detected in receipt");
            noItemsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff6b6b; -fx-padding: 20;");
            ingredientsBox.getChildren().add(noItemsLabel);
            
            Label suggestionLabel = new Label("Try uploading a clearer image or manually add items to your inventory.");
            suggestionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-padding: 0 20;");
            ingredientsBox.getChildren().add(suggestionLabel);
            
            // Show ingredients section anyway
            ingredientsSection.setVisible(true);
            ingredientsSection.setManaged(true);
            return;
        }
        
        // Create UI elements for each detected ingredient
        for (String item : detectedIngredients) {
            VBox itemBox = new VBox(5);
            itemBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
            
            // Top row with checkbox and item name
            HBox topRow = new HBox(10);
            topRow.setStyle("-fx-alignment: center-left;");
            
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(true); // Default to selected
            
            Label label = new Label(getIngredientEmoji(item) + " " + item);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            topRow.getChildren().addAll(checkBox, label);
            
            // Bottom row with quantity and expiration date
            HBox bottomRow = new HBox(15);
            bottomRow.setStyle("-fx-alignment: center-left;");
            
            // Quantity field
            Label qtyLabel = new Label("Qty:");
            qtyLabel.setStyle("-fx-font-size: 12px;");
            TextField quantityField = new TextField("1");
            quantityField.setPrefWidth(60);
            quantityField.setStyle("-fx-font-size: 12px;");
            
            // Expiration date picker
            Label expLabel = new Label("Expires:");
            expLabel.setStyle("-fx-font-size: 12px;");
            DatePicker datePicker = new DatePicker();
            datePicker.setValue(LocalDate.now().plusDays(getDefaultExpirationDays(item)));
            datePicker.setPrefWidth(140);
            datePicker.setStyle("-fx-font-size: 12px;");
            
            bottomRow.getChildren().addAll(qtyLabel, quantityField, expLabel, datePicker);
            
            itemBox.getChildren().addAll(topRow, bottomRow);
            ingredientsBox.getChildren().add(itemBox);
            
            ingredientCheckBoxes.add(checkBox);
            ingredientDatePickers.put(checkBox, datePicker);
            ingredientQuantityFields.put(checkBox, quantityField);
        }
        
        // Show ingredients section
        ingredientsSection.setVisible(true);
        ingredientsSection.setManaged(true);
        
        // Update drop zone text
        dropZone.getChildren().clear();
        Label successLabel = new Label("✅ Receipt processed successfully!");
        successLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: green;");
        Label detectedLabel = new Label("Found " + detectedIngredients.size() + " food items");
        detectedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        VBox successBox = new VBox(5);
        successBox.getChildren().addAll(successLabel, detectedLabel);
        successBox.setStyle("-fx-alignment: center;");
        dropZone.getChildren().add(successBox);
    }
    
    private int getDefaultExpirationDays(String ingredient) {
        // Return default expiration days based on ingredient type
        String lowerItem = ingredient.toLowerCase();
        
        if (lowerItem.contains("milk") || lowerItem.contains("yogurt") || lowerItem.contains("cream")) {
            return 7; // Dairy products
        } else if (lowerItem.contains("meat") || lowerItem.contains("chicken") || lowerItem.contains("beef") || 
                   lowerItem.contains("pork") || lowerItem.contains("fish") || lowerItem.contains("seafood")) {
            return 3; // Fresh meat
        } else if (lowerItem.contains("bread") || lowerItem.contains("bagel") || lowerItem.contains("roll")) {
            return 5; // Bread products
        } else if (lowerItem.contains("lettuce") || lowerItem.contains("spinach") || lowerItem.contains("kale") || 
                   lowerItem.contains("herbs") || lowerItem.contains("cilantro") || lowerItem.contains("parsley")) {
            return 4; // Leafy greens
        } else if (lowerItem.contains("banana") || lowerItem.contains("avocado") || lowerItem.contains("tomato")) {
            return 5; // Quick-ripening fruits
        } else if (lowerItem.contains("apple") || lowerItem.contains("orange") || lowerItem.contains("citrus")) {
            return 14; // Longer-lasting fruits
        } else if (lowerItem.contains("potato") || lowerItem.contains("onion") || lowerItem.contains("garlic") || 
                   lowerItem.contains("carrot") || lowerItem.contains("cabbage")) {
            return 21; // Root vegetables and hardy vegetables
        } else if (lowerItem.contains("egg")) {
            return 21; // Eggs
        } else if (lowerItem.contains("cheese")) {
            return 14; // Cheese
        } else {
            return 7; // Default for other items
        }
    }

    @FXML
    private void addSelectedToInventory() {
        String currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "No user logged in!");
            return;
        }

        int successCount = 0;
        for (CheckBox checkBox : ingredientCheckBoxes) {
            if (checkBox.isSelected()) {
                // Get the ingredient name from the label
                VBox itemBox = (VBox) checkBox.getParent().getParent(); // checkbox -> topRow -> itemBox
                HBox topRow = (HBox) checkBox.getParent();
                Label label = (Label) topRow.getChildren().get(1); // Second child is the label
                String ingredientText = label.getText();
                String ingredientName = ingredientText.substring(ingredientText.indexOf(" ") + 1).trim();
                
                // Get quantity and expiration date
                DatePicker datePicker = ingredientDatePickers.get(checkBox);
                TextField quantityField = ingredientQuantityFields.get(checkBox);
                
                try {
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    LocalDate expirationDate = datePicker.getValue();
                    
                    if (expirationDate == null) {
                        expirationDate = LocalDate.now().plusDays(7); // Default if no date selected
                    }
                    
                    if (DatabaseHelper.addToInventory(currentUser, ingredientName, quantity, expirationDate)) {
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid quantity for " + ingredientName + ": " + quantityField.getText());
                    // Still try to add with quantity 1
                    LocalDate expirationDate = datePicker.getValue();
                    if (expirationDate == null) {
                        expirationDate = LocalDate.now().plusDays(7);
                    }
                    if (DatabaseHelper.addToInventory(currentUser, ingredientName, 1, expirationDate)) {
                        successCount++;
                    }
                }
            }
        }

        if (successCount > 0) {
            showAlert("Success", "Added " + successCount + " ingredient(s) to your inventory!");
            // Reset the scanner
            resetScanner();
        } else {
            showAlert("Error", "Failed to add ingredients to inventory!");
        }
    }

    @FXML
    private void selectAllIngredients() {
        for (CheckBox checkBox : ingredientCheckBoxes) {
            checkBox.setSelected(true);
        }
    }

    @FXML
    private void clearAllIngredients() {
        // Clear the ingredients display
        ingredientsBox.getChildren().clear();
        ingredientCheckBoxes.clear();
        ingredientDatePickers.clear();
        ingredientQuantityFields.clear();
        
        // Hide the ingredients section if no ingredients are left
        ingredientsSection.setVisible(false);
        ingredientsSection.setManaged(false);
    }

    private void resetScanner() {
        ingredientsSection.setVisible(false);
        ingredientsSection.setManaged(false);
        dropZone.getChildren().clear();
        Label defaultLabel = new Label("Drag and Drop a file here or click to upload");
        defaultLabel.getStyleClass().add("drop-text");
        dropZone.getChildren().add(defaultLabel);
        ingredientCheckBoxes.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getIngredientEmoji(String ingredient) {
        String lowerIngredient = ingredient.toLowerCase();
        if (lowerIngredient.contains("carrot")) return "🥕";
        if (lowerIngredient.contains("tomato")) return "🍅";
        if (lowerIngredient.contains("spinach")) return "🥬";
        if (lowerIngredient.contains("broccoli")) return "🥦";
        if (lowerIngredient.contains("onion")) return "🧅";
        if (lowerIngredient.contains("garlic")) return "🧄";
        if (lowerIngredient.contains("pepper")) return "🌶️";
        if (lowerIngredient.contains("potato")) return "🥔";
        if (lowerIngredient.contains("mushroom")) return "🍄";
        if (lowerIngredient.contains("banana")) return "🍌";
        if (lowerIngredient.contains("apple")) return "🍎";
        if (lowerIngredient.contains("milk")) return "🥛";
        if (lowerIngredient.contains("egg")) return "🥚";
        if (lowerIngredient.contains("cheese")) return "🧀";
        if (lowerIngredient.contains("bread")) return "🍞";
        if (lowerIngredient.contains("chicken")) return "🍗";
        if (lowerIngredient.contains("rice")) return "🍚";
        if (lowerIngredient.contains("bean")) return "🥫";
        if (lowerIngredient.contains("zucchini")) return "🥒";
        if (lowerIngredient.contains("cabbage")) return "🥬";
        if (lowerIngredient.contains("kale")) return "🥬";
        if (lowerIngredient.contains("celery")) return "🥬";
        return "🥗"; // Default emoji for other ingredients
    }

    // 🔁 Navigation actions
    @FXML private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML private void goToInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML private void goToRecipes() throws IOException {
        App.setRoot("recipe");
    }

    @FXML private void goToShoppingList() throws IOException {
        App.setRoot("shopping_list");
    }

    @FXML private void goToStatistics() throws IOException {
        App.setRoot("statistics");
    }

    @FXML private void handleLogout() throws IOException {
        App.setRoot("start");
    }
}
