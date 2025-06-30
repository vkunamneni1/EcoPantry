package com.vedakunamneni.click.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.vedakunamneni.click.App;
import com.vedakunamneni.click.SessionManager;
import com.vedakunamneni.db.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class ShoppingListController implements Initializable {

    @FXML
    private VBox shoppingListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadShoppingList();
    }

    private void loadShoppingList() {
        if (shoppingListContainer == null) return;
        
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) return;
        
        shoppingListContainer.getChildren().clear();
        
        List<ShoppingListItem> shoppingList = DatabaseHelper.getShoppingListWithIds(userEmail);
        
        if (shoppingList.isEmpty()) {
            Label emptyLabel = new Label("Your shopping list is empty");
            emptyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-font-size: 16px; -fx-padding: 20;");
            shoppingListContainer.getChildren().add(emptyLabel);
        } else {
            for (ShoppingListItem item : shoppingList) {
                VBox itemBox = createShoppingListItemBox(item);
                shoppingListContainer.getChildren().add(itemBox);
            }
        }
    }

    private VBox createShoppingListItemBox(ShoppingListItem item) {
        VBox itemBox = new VBox(8);
        itemBox.getStyleClass().add("shopping-list-item");
        itemBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 12;");
        
        // Create main content row
        HBox mainRow = new HBox(10);
        mainRow.setStyle("-fx-alignment: center-left;");
        
        // Item label with emoji
        String emoji = getIngredientEmoji(item.ingredientName);
        Label itemLabel = new Label(emoji + " " + item.ingredientName + " (for " + item.recipeName + ")");
        itemLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 500;");
        
        // Spacer to push dropdown to the right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Create action dropdown
        ComboBox<String> actionDropdown = new ComboBox<>();
        actionDropdown.getItems().addAll("Select Action", "Remove", "Add to Inventory");
        actionDropdown.setValue("Select Action");
        actionDropdown.setStyle("-fx-font-size: 12px; -fx-padding: 5 10;");
        
        // Handle dropdown selection
        actionDropdown.setOnAction(e -> {
            String selectedAction = actionDropdown.getValue();
            if ("Remove".equals(selectedAction)) {
                removeShoppingListItem(item);
            } else if ("Add to Inventory".equals(selectedAction)) {
                addToInventoryFromShoppingList(item);
            }
            // Reset dropdown
            actionDropdown.setValue("Select Action");
        });
        
        mainRow.getChildren().addAll(itemLabel, spacer, actionDropdown);
        itemBox.getChildren().add(mainRow);
        
        return itemBox;
    }

    private void removeShoppingListItem(ShoppingListItem item) {
        if (DatabaseHelper.removeFromShoppingList(item.id)) {
            loadShoppingList(); // Refresh the list
        }
    }

    private void addToInventoryFromShoppingList(ShoppingListItem item) {
        // Show dialog to get quantity and expiration date
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add to Inventory");
        dialog.setHeaderText("Add " + item.ingredientName + " to your inventory");
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        quantityField.setText("1");
        
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(7)); // Default 7 days from now
        
        grid.add(new Label("Quantity:"), 0, 0);
        grid.add(quantityField, 1, 0);
        grid.add(new Label("Expiration Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Add buttons
        javafx.scene.control.ButtonType addButtonType = new javafx.scene.control.ButtonType("Add", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, javafx.scene.control.ButtonType.CANCEL);
        
        // Handle result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    LocalDate expirationDate = datePicker.getValue();
                    
                    if (quantity > 0 && expirationDate != null) {
                        String userEmail = SessionManager.getCurrentUser();
                        if (userEmail != null) {
                            // Add to inventory
                            boolean added = DatabaseHelper.addToInventory(userEmail, item.ingredientName, quantity, expirationDate);
                            if (added) {
                                // Remove from shopping list
                                DatabaseHelper.removeFromShoppingList(item.id);
                                return true;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid quantity
                }
            }
            return false;
        });
        
        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadShoppingList(); // Refresh the list
                showAlert("Success", item.ingredientName + " has been added to your inventory and removed from shopping list!");
            } else {
                showAlert("Error", "Failed to add item to inventory. Please check your input.");
            }
        });
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void clearAllShoppingList() {
        String userEmail = SessionManager.getCurrentUser();
        if (userEmail == null) return;
        
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Clear Shopping List");
        confirmAlert.setHeaderText("Remove all items from shopping list?");
        confirmAlert.setContentText("Are you sure you want to clear your entire shopping list?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (DatabaseHelper.clearShoppingList(userEmail)) {
                    loadShoppingList(); // Refresh the list
                }
            }
        });
    }

    // Navigation methods
    @FXML
    private void goToDashboard() throws IOException {
        App.setRoot("dashboard");
    }

    @FXML
    private void goToInventory() throws IOException {
        App.setRoot("inventory");
    }

    @FXML
    private void goToRecipes() throws IOException {
        App.setRoot("recipe");
    }

    @FXML
    private void goToScanner() throws IOException {
        App.setRoot("scanner");
    }

    @FXML
    private void goToStatistics() throws IOException {
        App.setRoot("statistics");
    }

    @FXML
    private void handleLogout() throws IOException {
        SessionManager.logout();
        App.setRoot("start");
    }

    // Inner class to represent shopping list items with IDs
    public static class ShoppingListItem {
        public final int id;
        public final String ingredientName;
        public final String recipeName;
        
        public ShoppingListItem(int id, String ingredientName, String recipeName) {
            this.id = id;
            this.ingredientName = ingredientName;
            this.recipeName = recipeName;
        }
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
        if (lowerIngredient.contains("raspberry")) return "🫐";
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
        if (lowerIngredient.contains("arugula")) return "🥬";
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
        if (lowerIngredient.contains("oat")) return "🥣";
        if (lowerIngredient.contains("quinoa")) return "🌾";
        if (lowerIngredient.contains("barley")) return "🌾";
        if (lowerIngredient.contains("wheat")) return "🌾";
        if (lowerIngredient.contains("flour")) return "🌾";
        if (lowerIngredient.contains("cereal")) return "🥣";
        if (lowerIngredient.contains("bagel")) return "🥯";
        if (lowerIngredient.contains("croissant")) return "🥐";
        if (lowerIngredient.contains("pretzel")) return "🥨";
        
        // Proteins
        if (lowerIngredient.contains("chicken")) return "🍗";
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
        if (lowerIngredient.contains("feta")) return "🧀";
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
        if (lowerIngredient.contains("pecan")) return "🌰";
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
        if (lowerIngredient.contains("rosemary")) return "🌿";
        if (lowerIngredient.contains("sage")) return "🌿";
        if (lowerIngredient.contains("mint")) return "🌿";
        if (lowerIngredient.contains("cilantro")) return "🌿";
        if (lowerIngredient.contains("parsley")) return "🌿";
        if (lowerIngredient.contains("dill")) return "🌿";
        if (lowerIngredient.contains("tarragon")) return "🌿";
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
        if (lowerIngredient.contains("olive oil")) return "🫒";
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
        if (lowerIngredient.contains("peanut butter")) return "🥜";
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
        if (lowerIngredient.contains("soda")) return "🥤";
        if (lowerIngredient.contains("sparkling water")) return "🫧";
        
        // Sweets & Desserts
        if (lowerIngredient.contains("chocolate")) return "🍫";
        if (lowerIngredient.contains("cocoa")) return "🍫";
        if (lowerIngredient.contains("sugar")) return "🧂";
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
        if (lowerIngredient.contains("oat milk")) return "🥣";
        if (lowerIngredient.contains("soy milk")) return "🟡";
        if (lowerIngredient.contains("broth")) return "🍲";
        if (lowerIngredient.contains("stock")) return "🍲";
        
        return "🥗"; // Default emoji for other ingredients
    }
}
