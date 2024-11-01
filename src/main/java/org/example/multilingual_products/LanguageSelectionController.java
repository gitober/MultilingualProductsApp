package org.example.multilingual_products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ResourceBundle;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LanguageSelectionController {

    @FXML private Button loadButton;
    @FXML private Button addButton;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> descriptionColumn;
    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private Label languageLabel;
    @FXML private Label messageLabel;

    private ResourceBundle bundle;

    public void initialize() {
        languageComboBox.getItems().addAll("English", "French", "Spanish", "German");
        languageComboBox.setValue("English");

        updateLanguage("en");
        clearTableContent();

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
    }

    private void updateLanguage(String languageCode) {
        Locale locale = switch (languageCode) {
            case "fr" -> Locale.FRENCH;
            case "es" -> new Locale("es", "ES");
            case "de" -> Locale.GERMAN;
            default -> Locale.ENGLISH;
        };
        bundle = ResourceBundle.getBundle("messages", locale);

        languageLabel.setText(bundle.getString("selectLanguage"));
        loadButton.setText(bundle.getString("loadProducts"));
        addButton.setText(bundle.getString("addProduct"));
        idColumn.setText(bundle.getString("id"));
        nameColumn.setText(bundle.getString("name"));
        descriptionColumn.setText(bundle.getString("description"));
        messageLabel.setText(bundle.getString("instructionMessage"));
    }

    @FXML
    private void onLanguageSelection() {
        String languageCode = getLanguageCode(languageComboBox.getValue());
        updateLanguage(languageCode);
        clearTableContent();
    }

    @FXML
    private void onLoadProducts() {
        String languageCode = getLanguageCode(languageComboBox.getValue());
        String tableName = "product_" + languageCode;
        System.out.println("Loading products from table: " + tableName);
        loadProducts(tableName);
    }

    @FXML
    private void onAddProduct() {
        String selectedLanguage = languageComboBox.getValue();
        String languageCode = getLanguageCode(selectedLanguage);
        String tableName = "product_" + languageCode;

        try {
            String name = nameField.getText();
            String description = descriptionField.getText();

            addProductToDatabase(tableName, name, description);
            loadProducts(tableName);
            clearInputFields();

        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getLanguageCode(String language) {
        return switch (language) {
            case "French" -> "fr";
            case "Spanish" -> "es";
            case "German" -> "de";
            default -> "en";
        };
    }

    private void addProductToDatabase(String tableName, String name, String description) {
        String sql = "INSERT INTO " + tableName + " (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts(String tableName) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT id, name, description FROM " + tableName;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }

            if (products.isEmpty()) {
                messageLabel.setText("No content available.");
            } else {
                messageLabel.setText("");
            }

            productTable.setItems(products);
            productTable.refresh();

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void clearTableContent() {
        productTable.getItems().clear();
    }

    private void clearInputFields() {
        nameField.clear();
        descriptionField.clear();
    }
}
