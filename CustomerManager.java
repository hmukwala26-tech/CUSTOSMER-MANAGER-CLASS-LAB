package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CustomerManager extends Application {

    // Step 2: Customer model (properties so TableView can bind to it)
    public static class Customer {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty province = new SimpleStringProperty();

        public Customer(String name, String province) {
            this.name.set(name);
            this.province.set(province);
        }
        public String getName() { return name.get(); }
        public String getProvince() { return province.get(); }
        public StringProperty nameProperty() { return name; }
        public StringProperty provinceProperty() { return province; }
    }

    // Step 2: observable list backing the table
    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // ---- Step 1: form (name field + province list) ----
        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        Label nameLabel = new Label("_Name:");
        nameLabel.setMnemonicParsing(true);
        nameLabel.setLabelFor(nameField);

        ComboBox<String> provinceBox = new ComboBox<>(FXCollections.observableArrayList(
                "Central", "Copperbelt", "Eastern", "Luapula", "Lusaka",
                "Muchinga", "Northern", "North-Western", "Southern", "Western"));
        provinceBox.setPromptText("Select province");
        Label provinceLabel = new Label("_Province:");
        provinceLabel.setMnemonicParsing(true);
        provinceLabel.setLabelFor(provinceBox);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setWrapText(true);

        Button addBtn = new Button("_Add");
        addBtn.setDefaultButton(true);          // Enter triggers Add
        Button deleteBtn = new Button("_Delete");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, nameLabel, nameField);
        form.addRow(1, provinceLabel, provinceBox);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        provinceBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(provinceBox, Priority.ALWAYS);

        // ---- Step 3: TableView with name and province columns ----
        TableView<Customer> table = new TableView<>(customers);
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        TableColumn<Customer, String> provCol = new TableColumn<>("Province");
        provCol.setCellValueFactory(c -> c.getValue().provinceProperty());
        table.getColumns().add(nameCol);
        table.getColumns().add(provCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No customers yet"));
        VBox.setVgrow(table, Priority.ALWAYS);

        // ---- Step 4: validate, then add ----
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String province = provinceBox.getValue();

            if (name.isEmpty()) {
                showError(errorLabel, "Name is required.", nameField);
                return;
            }
            if (!name.matches("[\\p{L}][\\p{L} .'-]*")) {
                showError(errorLabel, "Name may contain only letters, spaces, . ' and -.", nameField);
                return;
            }
            if (province == null) {
                showError(errorLabel, "Please select a province.", provinceBox);
                return;
            }
            boolean duplicate = customers.stream().anyMatch(
                    c -> c.getName().equalsIgnoreCase(name) && c.getProvince().equals(province));
            if (duplicate) {
                showError(errorLabel, "That customer already exists.", nameField);
                return;
            }

            customers.add(new Customer(name, province));
            errorLabel.setText("");
            nameField.clear();
            provinceBox.setValue(null);
            nameField.requestFocus();
        });

        // ---- Step 5: confirm deletion of selected customer ----
        deleteBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        Runnable deleteSelected = () -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + selected.getName() + " (" + selected.getProvince() + ")?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm deletion");
            confirm.setHeaderText(null);
            confirm.showAndWait()
                    .filter(b -> b == ButtonType.YES)
                    .ifPresent(b -> customers.remove(selected));
        };
        deleteBtn.setOnAction(e -> deleteSelected.run());
        // Step 6: keyboard access - Delete key on the table
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) deleteSelected.run();
        });

        HBox buttons = new HBox(10, addBtn, deleteBtn);
        VBox root = new VBox(12, form, errorLabel, buttons, table);
        root.setPadding(new Insets(15));

        stage.setTitle("Customer Manager");
        stage.setScene(new Scene(root, 480, 460));
        stage.show();
        nameField.requestFocus();
    }

    private void showError(Label errorLabel, String msg, Control focusTarget) {
        errorLabel.setText(msg);
        focusTarget.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}