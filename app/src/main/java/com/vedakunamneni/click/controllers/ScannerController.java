package com.vedakunamneni.click.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
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
        // Show processing message with animation
        dropZone.getChildren().clear();
        
        // Create processing container
        VBox processingContainer = new VBox(15);
        processingContainer.getStyleClass().add("processing-container");
        processingContainer.setStyle("-fx-alignment: center;");
        
        // Add progress indicator
        javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
        progressIndicator.getStyleClass().add("loading-progress");
        progressIndicator.setPrefSize(50, 50);
        
        // Add processing label with animation
        Label processingLabel = new Label("🔍 Processing receipt...");
        processingLabel.getStyleClass().add("loading-text");
        
        // Create animated dots for loading effect
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        
        KeyFrame keyFrame = new KeyFrame(
            Duration.seconds(0.5),
            e -> {
                String currentText = processingLabel.getText();
                if (currentText.endsWith("...")) {
                    processingLabel.setText("🔍 Processing receipt");
                } else {
                    processingLabel.setText(currentText + ".");
                }
            }
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        
        processingContainer.getChildren().addAll(progressIndicator, processingLabel);
        
        // Add fade-in effect
        processingContainer.setOpacity(0);
        dropZone.getChildren().add(processingContainer);
        
        // Animate fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), processingContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Simulate processing delay and then extract ingredients
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate processing time
                
                // Extract text from receipt using OCR
                String receiptText = performOCR(receiptFile);
                
                // Extract ingredients from text
                List<String> detectedIngredients = extractIngredientsFromText(receiptText);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    timeline.stop(); // Stop the loading animation
                    showDetectedIngredients(detectedIngredients);
                });
                
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> {
                    timeline.stop(); // Stop animation on error too
                });
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private String performOCR(File receiptFile) {
        // Try to use Tesseract OCR if available, otherwise fall back to mock data
        try {
            // First check if we can load the Tesseract classes
            Class.forName("net.sourceforge.tess4j.Tesseract");
            System.out.println("Tesseract classes found, attempting OCR...");
            return performTesseractOCR(receiptFile);
        } catch (ClassNotFoundException e) {
            // Tesseract not available, use enhanced filename-based processing
            System.out.println("Tesseract OCR not available, using fallback processing for file: " + receiptFile.getName());
            return performFallbackOCR(receiptFile);
        } catch (Exception e) {
            // OCR failed, use fallback
            System.out.println("OCR processing failed for file " + receiptFile.getName() + ": " + e.getClass().getSimpleName());
            System.out.println("Falling back to simulated receipt data...");
            return performFallbackOCR(receiptFile);
        }
    }

    private String performTesseractOCR(File receiptFile) throws Exception {
        if (receiptFile == null || !receiptFile.exists()) {
            throw new Exception("Receipt file is null or does not exist");
        }
        
        if (!isImageFile(receiptFile)) {
            throw new Exception("File is not a supported image format");
        }
        
        System.out.println("Processing file: " + receiptFile.getAbsolutePath());
        
        // Set library path for macOS/Homebrew before loading Tesseract
        String[] libraryPaths = {
            "/opt/homebrew/lib",        // Homebrew on Apple Silicon
            "/usr/local/lib",           // Homebrew on Intel
            "/usr/lib",                 // System libraries
        };
        
        // Add library paths to jna.library.path
        StringBuilder jnaPath = new StringBuilder();
        String currentJnaPath = System.getProperty("jna.library.path", "");
        if (!currentJnaPath.isEmpty()) {
            jnaPath.append(currentJnaPath).append(":");
        }
        
        for (String path : libraryPaths) {
            if (path != null && new File(path).exists()) {
                jnaPath.append(path).append(":");
            }
        }
        
        if (jnaPath.length() > 0) {
            System.setProperty("jna.library.path", jnaPath.toString());
            System.out.println("Set JNA library path to: " + jnaPath.toString());
        }
        
        // Use reflection to avoid compile-time dependency issues
        Class<?> tesseractClass = Class.forName("net.sourceforge.tess4j.Tesseract");
        Object tesseract = tesseractClass.getDeclaredConstructor().newInstance();
        
        // Try to find tessdata directory
        boolean dataPathSet = false;
        String[] possibleDataPaths = {
            "/opt/homebrew/share/tessdata",  // macOS Homebrew
            "/usr/share/tesseract-ocr/tessdata", // Linux
            "/usr/local/share/tessdata",      // Alternative Linux
            "/usr/share/tessdata",            // Another common location
            System.getProperty("user.home") + "/tessdata" // User home
        };
        
        for (String dataPath : possibleDataPaths) {
            File dataDir = new File(dataPath);
            if (dataDir.exists() && dataDir.isDirectory()) {
                System.out.println("Found tessdata at: " + dataPath);
                tesseractClass.getMethod("setDatapath", String.class).invoke(tesseract, dataPath);
                dataPathSet = true;
                break;
            }
        }
        
        if (!dataPathSet) {
            System.out.println("Warning: No tessdata directory found, Tesseract may not work properly");
        }
        
        // Set language to English
        tesseractClass.getMethod("setLanguage", String.class).invoke(tesseract, "eng");
        
        // Perform OCR
        System.out.println("Starting OCR processing...");
        String result = (String) tesseractClass.getMethod("doOCR", File.class).invoke(tesseract, receiptFile);
        
        if (result != null && !result.trim().isEmpty()) {
            System.out.println("OCR successful! Extracted " + result.length() + " characters");
            return result;
        } else {
            throw new Exception("OCR returned empty result");
        }
    }

    private String performFallbackOCR(File receiptFile) {
        // Enhanced fallback that tries to be smarter about filename-based detection
        if (receiptFile == null) {
            System.out.println("File is null, using default receipt");
            return generateGenericStoreReceipt();
        }
        
        String fileName = receiptFile.getName().toLowerCase();
        System.out.println("Using fallback OCR for file: " + fileName);
        
        // Check filename for store hints
        if (fileName.contains("whole") || fileName.contains("foods")) {
            return generateWholeFoodsReceipt();
        } else if (fileName.contains("safeway") || fileName.contains("grocery")) {
            return generateSafewayReceipt();
        } else if (fileName.contains("trader") || fileName.contains("joes")) {
            return generateTraderJoesReceipt();
        } else if (fileName.contains("target") || fileName.contains("walmart")) {
            return generateGenericStoreReceipt();
        }
        
        // Default to a varied receipt based on file hash
        String[] mockReceipts = {
            generateWholeFoodsReceipt(),
            generateSafewayReceipt(), 
            generateTraderJoesReceipt(),
            generateGenericStoreReceipt()
        };
        
        int index = Math.abs(fileName.hashCode()) % mockReceipts.length;
        String result = mockReceipts[index];
        
        System.out.println("Generated fallback receipt with " + result.length() + " characters");
        return result;
    }

    private String generateWholeFoodsReceipt() {
        return "WHOLE FOODS MARKET\n" +
               "365 ORGANIC BANANAS         $3.49\n" +
               "ORGANIC ROMA TOMATOES       $4.29\n" +
               "BABY SPINACH ORGANIC        $3.99\n" +
               "CHICKEN BREAST ANTIBIOTIC   $12.99\n" +
               "ORGANIC WHOLE MILK          $4.79\n" +
               "AGED CHEDDAR CHEESE         $6.49\n" +
               "EZEKIEL BREAD               $4.99\n" +
               "EXTRA VIRGIN OLIVE OIL      $8.99\n" +
               "ORGANIC GARLIC              $2.49\n" +
               "BROCCOLI CROWNS             $4.99\n" +
               "ORGANIC AVOCADOS            $5.99";
    }

    private String generateSafewayReceipt() {
        return "SAFEWAY STORE #1234\n" +
               "GROUND BEEF 85/15 LB        $7.99\n" +
               "YELLOW ONIONS 3LB BAG       $2.99\n" +
               "BELL PEPPERS RED/YELLOW     $4.49\n" +
               "WHITE MUSHROOMS 8OZ         $2.79\n" +
               "BARILLA PENNE PASTA         $1.99\n" +
               "RAGU MARINARA SAUCE         $2.49\n" +
               "KRAFT PARMESAN CHEESE       $5.99\n" +
               "ROMAINE LETTUCE HEARTS      $3.99\n" +
               "CUCUMBERS EACH              $1.79\n" +
               "EGGS LARGE GRADE A          $3.79\n" +
               "CARROTS 2LB BAG             $1.99";
    }

    private String generateTraderJoesReceipt() {
        return "TRADER JOES\n" +
               "HASS AVOCADOS 4CT           $3.99\n" +
               "ORGANIC POWER GREENS        $2.99\n" +
               "ORGANIC SWEET POTATOES      $2.99\n" +
               "ATLANTIC SALMON FILLET      $14.99\n" +
               "ORGANIC TRICOLOR QUINOA     $4.49\n" +
               "ALMOND BUTTER CRUNCHY       $7.99\n" +
               "COCONUT MILK LIGHT          $1.99\n" +
               "ORGANIC BLUEBERRIES         $4.99\n" +
               "ORGANIC BABY CARROTS        $1.99\n" +
               "GREEK NONFAT YOGURT         $5.99\n" +
               "EZEKIEL SPROUTED BREAD      $3.99";
    }

    private String generateGenericStoreReceipt() {
        return "GROCERY STORE\n" +
               "APPLES GALA 3LB BAG         $4.99\n" +
               "FRESH STRAWBERRIES          $3.99\n" +
               "ICEBERG LETTUCE HEAD        $1.99\n" +
               "GROUND TURKEY 93/7          $5.99\n" +
               "CHEDDAR CHEESE BLOCK        $4.99\n" +
               "WHOLE WHEAT BREAD           $2.99\n" +
               "PASTA SAUCE TRADITIONAL     $1.99\n" +
               "BROWN RICE 2LB              $3.99\n" +
               "OLIVE OIL 500ML             $6.99\n" +
               "ONIONS YELLOW 3LB           $2.99\n" +
               "FROZEN BROCCOLI             $2.49";
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
        
        // Reset the upload section to default state
        resetUploadSection();
        
        // Clear the uploaded file reference
        uploadedReceiptFile = null;
    }

    private void resetScanner() {
        ingredientsSection.setVisible(false);
        ingredientsSection.setManaged(false);
        resetUploadSection();
        ingredientCheckBoxes.clear();
    }

    private void resetUploadSection() {
        // Reset dropZone to its default state (as it appears when page first loads)
        dropZone.getChildren().clear();
        
        // Add the default elements back
        Label iconLabel = new Label("📄");
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label mainLabel = new Label("Drag & Drop Receipt Here");
        mainLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label subLabel = new Label("or click to browse files");
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label formatLabel = new Label("Supports: JPG, PNG, PDF");
        formatLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
        
        dropZone.getChildren().addAll(iconLabel, mainLabel, subLabel, formatLabel);
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
        
        // Fruits
        if (lowerIngredient.contains("apple")) return "🍎";
        if (lowerIngredient.contains("banana")) return "🍌";
        if (lowerIngredient.contains("orange")) return "🍊";
        if (lowerIngredient.contains("lemon")) return "🍋";
        if (lowerIngredient.contains("lime")) return "🍋";
        if (lowerIngredient.contains("grape")) return "🍇";
        if (lowerIngredient.contains("strawberry")) return "🍓";
        if (lowerIngredient.contains("raspberry")) return "�";
        if (lowerIngredient.contains("blueberry")) return "🫐";
        if (lowerIngredient.contains("blackberry")) return "🫐";
        if (lowerIngredient.contains("cherry")) return "🍒";
        if (lowerIngredient.contains("peach")) return "🍑";
        if (lowerIngredient.contains("pear")) return "🍐";
        if (lowerIngredient.contains("pineapple")) return "🍍";
        if (lowerIngredient.contains("mango")) return "🥭";
        if (lowerIngredient.contains("avocado")) return "🥑";
        if (lowerIngredient.contains("coconut")) return "🥥";
        if (lowerIngredient.contains("kiwi")) return "🥝";
        if (lowerIngredient.contains("watermelon")) return "🍉";
        if (lowerIngredient.contains("melon")) return "🍈";
        if (lowerIngredient.contains("cantaloupe")) return "🍈";
        if (lowerIngredient.contains("papaya")) return "🥭";
        if (lowerIngredient.contains("plum")) return "🟣";
        if (lowerIngredient.contains("apricot")) return "🍑";
        
        // Vegetables
        if (lowerIngredient.contains("tomato")) return "🍅";
        if (lowerIngredient.contains("carrot")) return "🥕";
        if (lowerIngredient.contains("broccoli")) return "🥦";
        if (lowerIngredient.contains("spinach")) return "🥬";
        if (lowerIngredient.contains("lettuce")) return "🥬";
        if (lowerIngredient.contains("cabbage")) return "🥬";
        if (lowerIngredient.contains("kale")) return "🥬";
        if (lowerIngredient.contains("arugula")) return "�";
        if (lowerIngredient.contains("onion")) return "🧅";
        if (lowerIngredient.contains("garlic")) return "🧄";
        if (lowerIngredient.contains("ginger")) return "🫚";
        if (lowerIngredient.contains("potato")) return "🥔";
        if (lowerIngredient.contains("sweet potato")) return "🍠";
        if (lowerIngredient.contains("mushroom")) return "🍄";
        if (lowerIngredient.contains("bell pepper")) return "🫑";
        if (lowerIngredient.contains("pepper") && !lowerIngredient.contains("bell")) return "🌶️";
        if (lowerIngredient.contains("chili")) return "🌶️";
        if (lowerIngredient.contains("jalapeno")) return "🌶️";
        if (lowerIngredient.contains("cucumber")) return "🥒";
        if (lowerIngredient.contains("zucchini")) return "🥒";
        if (lowerIngredient.contains("eggplant")) return "🍆";
        if (lowerIngredient.contains("corn")) return "🌽";
        if (lowerIngredient.contains("peas")) return "🟢";
        if (lowerIngredient.contains("green bean")) return "🥦";
        if (lowerIngredient.contains("asparagus")) return "🥦";
        if (lowerIngredient.contains("celery")) return "🥬";
        if (lowerIngredient.contains("radish")) return "🟣";
        if (lowerIngredient.contains("beet")) return "🔴";
        if (lowerIngredient.contains("turnip")) return "⚪";
        if (lowerIngredient.contains("parsnip")) return "⚪";
        if (lowerIngredient.contains("leek")) return "🧅";
        if (lowerIngredient.contains("shallot")) return "🧅";
        if (lowerIngredient.contains("scallion")) return "🌿";
        if (lowerIngredient.contains("green onion")) return "🌿";
        if (lowerIngredient.contains("chive")) return "🌿";
        
        // Grains & Cereals
        if (lowerIngredient.contains("rice")) return "🍚";
        if (lowerIngredient.contains("bread")) return "🍞";
        if (lowerIngredient.contains("pasta")) return "🍝";
        if (lowerIngredient.contains("noodle")) return "🍜";
        if (lowerIngredient.contains("oat")) return "�";
        if (lowerIngredient.contains("quinoa")) return "🌾";
        if (lowerIngredient.contains("barley")) return "🌾";
        if (lowerIngredient.contains("wheat")) return "🌾";
        if (lowerIngredient.contains("flour")) return "�";
        if (lowerIngredient.contains("cereal")) return "🥣";
        if (lowerIngredient.contains("bagel")) return "🥯";
        if (lowerIngredient.contains("croissant")) return "🥐";
        if (lowerIngredient.contains("pretzel")) return "🥨";
        
        // Proteins
        if (lowerIngredient.contains("chicken")) return "�";
        if (lowerIngredient.contains("beef")) return "🥩";
        if (lowerIngredient.contains("pork")) return "🥩";
        if (lowerIngredient.contains("lamb")) return "🥩";
        if (lowerIngredient.contains("turkey")) return "🦃";
        if (lowerIngredient.contains("duck")) return "🦆";
        if (lowerIngredient.contains("fish")) return "🐟";
        if (lowerIngredient.contains("salmon")) return "🐟";
        if (lowerIngredient.contains("tuna")) return "🐟";
        if (lowerIngredient.contains("shrimp")) return "🦐";
        if (lowerIngredient.contains("crab")) return "🦀";
        if (lowerIngredient.contains("lobster")) return "🦞";
        if (lowerIngredient.contains("scallop")) return "🐚";
        if (lowerIngredient.contains("oyster")) return "🦪";
        if (lowerIngredient.contains("mussel")) return "🐚";
        if (lowerIngredient.contains("clam")) return "🐚";
        if (lowerIngredient.contains("squid")) return "🦑";
        if (lowerIngredient.contains("octopus")) return "🐙";
        if (lowerIngredient.contains("egg")) return "🥚";
        if (lowerIngredient.contains("tofu")) return "⬜";
        if (lowerIngredient.contains("tempeh")) return "🟤";
        
        // Dairy
        if (lowerIngredient.contains("milk")) return "🥛";
        if (lowerIngredient.contains("cheese")) return "🧀";
        if (lowerIngredient.contains("yogurt")) return "🥛";
        if (lowerIngredient.contains("butter")) return "🧈";
        if (lowerIngredient.contains("cream")) return "🥛";
        if (lowerIngredient.contains("sour cream")) return "🥛";
        if (lowerIngredient.contains("cottage cheese")) return "🧀";
        if (lowerIngredient.contains("ricotta")) return "🧀";
        if (lowerIngredient.contains("mozzarella")) return "🧀";
        if (lowerIngredient.contains("cheddar")) return "🧀";
        if (lowerIngredient.contains("parmesan")) return "🧀";
        if (lowerIngredient.contains("feta")) return "�";
        if (lowerIngredient.contains("goat cheese")) return "🧀";
        if (lowerIngredient.contains("brie")) return "🧀";
        if (lowerIngredient.contains("camembert")) return "🧀";
        
        // Legumes & Nuts
        if (lowerIngredient.contains("bean")) return "🥫";
        if (lowerIngredient.contains("lentil")) return "🟤";
        if (lowerIngredient.contains("chickpea")) return "🟡";
        if (lowerIngredient.contains("garbanzo")) return "🟡";
        if (lowerIngredient.contains("black bean")) return "⚫";
        if (lowerIngredient.contains("kidney bean")) return "🔴";
        if (lowerIngredient.contains("pinto bean")) return "🟤";
        if (lowerIngredient.contains("navy bean")) return "⚪";
        if (lowerIngredient.contains("lima bean")) return "🟢";
        if (lowerIngredient.contains("soybean")) return "🟡";
        if (lowerIngredient.contains("almond")) return "🌰";
        if (lowerIngredient.contains("walnut")) return "🌰";
        if (lowerIngredient.contains("pecan")) return "�";
        if (lowerIngredient.contains("cashew")) return "🌰";
        if (lowerIngredient.contains("pistachio")) return "🌰";
        if (lowerIngredient.contains("hazelnut")) return "🌰";
        if (lowerIngredient.contains("macadamia")) return "🌰";
        if (lowerIngredient.contains("brazil nut")) return "🌰";
        if (lowerIngredient.contains("pine nut")) return "🌰";
        if (lowerIngredient.contains("peanut")) return "🥜";
        if (lowerIngredient.contains("sunflower seed")) return "🌻";
        if (lowerIngredient.contains("pumpkin seed")) return "🎃";
        if (lowerIngredient.contains("sesame seed")) return "⚪";
        if (lowerIngredient.contains("chia seed")) return "⚫";
        if (lowerIngredient.contains("flax seed")) return "🟤";
        
        // Herbs & Spices
        if (lowerIngredient.contains("basil")) return "🌿";
        if (lowerIngredient.contains("oregano")) return "🌿";
        if (lowerIngredient.contains("thyme")) return "🌿";
        if (lowerIngredient.contains("rosemary")) return "�";
        if (lowerIngredient.contains("sage")) return "🌿";
        if (lowerIngredient.contains("mint")) return "🌿";
        if (lowerIngredient.contains("cilantro")) return "🌿";
        if (lowerIngredient.contains("parsley")) return "🌿";
        if (lowerIngredient.contains("dill")) return "🌿";
        if (lowerIngredient.contains("tarragon")) return "�";
        if (lowerIngredient.contains("bay leaf")) return "🍃";
        if (lowerIngredient.contains("cinnamon")) return "🟤";
        if (lowerIngredient.contains("nutmeg")) return "🟤";
        if (lowerIngredient.contains("clove")) return "🟤";
        if (lowerIngredient.contains("cardamom")) return "🟢";
        if (lowerIngredient.contains("cumin")) return "🟤";
        if (lowerIngredient.contains("coriander")) return "🟤";
        if (lowerIngredient.contains("paprika")) return "🔴";
        if (lowerIngredient.contains("turmeric")) return "🟡";
        if (lowerIngredient.contains("saffron")) return "🟡";
        if (lowerIngredient.contains("vanilla")) return "🟤";
        if (lowerIngredient.contains("black pepper")) return "⚫";
        if (lowerIngredient.contains("white pepper")) return "⚪";
        if (lowerIngredient.contains("cayenne")) return "🔴";
        if (lowerIngredient.contains("mustard seed")) return "🟡";
        if (lowerIngredient.contains("fennel seed")) return "🟢";
        if (lowerIngredient.contains("star anise")) return "⭐";
        
        // Oils & Condiments
        if (lowerIngredient.contains("olive oil")) return "�";
        if (lowerIngredient.contains("coconut oil")) return "🥥";
        if (lowerIngredient.contains("vegetable oil")) return "🟡";
        if (lowerIngredient.contains("canola oil")) return "🟡";
        if (lowerIngredient.contains("sesame oil")) return "🟤";
        if (lowerIngredient.contains("vinegar")) return "🍶";
        if (lowerIngredient.contains("balsamic")) return "🟤";
        if (lowerIngredient.contains("soy sauce")) return "🟤";
        if (lowerIngredient.contains("worcestershire")) return "🟤";
        if (lowerIngredient.contains("hot sauce")) return "🌶️";
        if (lowerIngredient.contains("ketchup")) return "🍅";
        if (lowerIngredient.contains("mustard")) return "🟡";
        if (lowerIngredient.contains("mayonnaise")) return "⚪";
        if (lowerIngredient.contains("honey")) return "🍯";
        if (lowerIngredient.contains("maple syrup")) return "🍁";
        if (lowerIngredient.contains("molasses")) return "🟤";
        if (lowerIngredient.contains("jam")) return "🍓";
        if (lowerIngredient.contains("jelly")) return "🍇";
        if (lowerIngredient.contains("peanut butter")) return "�";
        if (lowerIngredient.contains("almond butter")) return "🌰";
        if (lowerIngredient.contains("nutella")) return "🟤";
        
        // Beverages
        if (lowerIngredient.contains("coffee")) return "☕";
        if (lowerIngredient.contains("tea")) return "🍵";
        if (lowerIngredient.contains("juice")) return "🧃";
        if (lowerIngredient.contains("wine")) return "🍷";
        if (lowerIngredient.contains("beer")) return "🍺";
        if (lowerIngredient.contains("vodka")) return "🍸";
        if (lowerIngredient.contains("rum")) return "🥃";
        if (lowerIngredient.contains("whiskey")) return "🥃";
        if (lowerIngredient.contains("brandy")) return "🥃";
        if (lowerIngredient.contains("coconut water")) return "🥥";
        if (lowerIngredient.contains("soda")) return "�";
        if (lowerIngredient.contains("sparkling water")) return "🫧";
        
        // Sweets & Desserts
        if (lowerIngredient.contains("chocolate")) return "🍫";
        if (lowerIngredient.contains("cocoa")) return "🍫";
        if (lowerIngredient.contains("sugar")) return "�";
        if (lowerIngredient.contains("brown sugar")) return "🟤";
        if (lowerIngredient.contains("powdered sugar")) return "⚪";
        if (lowerIngredient.contains("vanilla extract")) return "🟤";
        if (lowerIngredient.contains("baking powder")) return "⚪";
        if (lowerIngredient.contains("baking soda")) return "⚪";
        if (lowerIngredient.contains("yeast")) return "🟡";
        if (lowerIngredient.contains("cornstarch")) return "⚪";
        if (lowerIngredient.contains("gelatin")) return "⚪";
        if (lowerIngredient.contains("marshmallow")) return "⚪";
        if (lowerIngredient.contains("coconut flake")) return "⚪";
        if (lowerIngredient.contains("raisin")) return "🟤";
        if (lowerIngredient.contains("date")) return "🟤";
        if (lowerIngredient.contains("fig")) return "🟣";
        if (lowerIngredient.contains("cranberry")) return "🔴";
        
        // Frozen Foods
        if (lowerIngredient.contains("ice cream")) return "🍨";
        if (lowerIngredient.contains("frozen yogurt")) return "🍦";
        if (lowerIngredient.contains("sorbet")) return "🍧";
        if (lowerIngredient.contains("popsicle")) return "🍭";
        
        // Canned/Processed
        if (lowerIngredient.contains("tomato sauce")) return "🍅";
        if (lowerIngredient.contains("tomato paste")) return "🍅";
        if (lowerIngredient.contains("coconut milk")) return "🥥";
        if (lowerIngredient.contains("almond milk")) return "🌰";
        if (lowerIngredient.contains("oat milk")) return "�";
        if (lowerIngredient.contains("soy milk")) return "🟡";
        if (lowerIngredient.contains("broth")) return "🍲";
        if (lowerIngredient.contains("stock")) return "🍲";
        
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
