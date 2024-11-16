package com.example.course;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.bson.Document;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class appcontroller implements Initializable {

    @FXML
    private TextField Age_ID, Email_ID, Full_name_ID, Username_ID, Username_text;

    @FXML
    private PasswordField Confirmpw_ID, Password_ID, password_text;

    @FXML
    private CheckBox Entertainment_ID, Lifestyle_and_Culture_ID, Politics_ID, Science_ID, Sport_ID, Tech_ID;

    @FXML
    private Button Sign_Up_page, Sign_in, back_to_main, data_sign_up;

    @FXML
    private ImageView Close_app;

    @FXML
    private Pane Signup_menu, mainpane;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection, loginCollection, adminCollection;

    @FXML
    private void control_signin(ActionEvent event) {
        String username = Username_text.getText();
        String password = password_text.getText();

        if (checkIfAdmin(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Login", "Welcome admin " + username);
            goToAdminPage();
        } else if (checkCredentials(username, password)) {
            saveLoginDetails(username);
            showAlert(Alert.AlertType.INFORMATION, "Login", "Welcome " + username);
            goToUserPage();
        } else {
            showAlert(Alert.AlertType.ERROR, "Login", "Incorrect username or password");
        }
    }

    private boolean checkCredentials(String username, String password) {
        Document user = userCollection.find(Filters.and(Filters.eq("Username", username), Filters.eq("Password", password))).first();
        return user != null;
    }

    private boolean checkIfAdmin(String username, String password) {
        Document admin = adminCollection.find(Filters.and(Filters.eq("Username", username), Filters.eq("Password", password))).first();
        return admin != null;
    }

    private void saveLoginDetails(String username) {
        Document loginDetail = new Document("Username", username).append("Login_time", LocalDateTime.now().toString());
        loginCollection.insertOne(loginDetail);
    }

    private void goToAdminPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-page.fxml"));
            Parent adminPageRoot = loader.load();
            Scene adminScene = new Scene(adminPageRoot);
            Stage stage = (Stage) Username_text.getScene().getWindow();
            stage.setScene(adminScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the admin page.");
        }
    }

    private void goToUserPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-page.fxml"));
            Parent userPageRoot = loader.load();
            Scene userScene = new Scene(userPageRoot);
            Stage stage = (Stage) Username_text.getScene().getWindow();
            stage.setScene(userScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the user page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void validateAndRegisterUser(ActionEvent event) {
        if (event.getSource() == data_sign_up) {  // Confirm that this action is only triggered by data_sign_up button
            String email = Email_ID.getText().trim();
            String fullName = Full_name_ID.getText().trim();
            String ageText = Age_ID.getText().trim();
            String username = Username_ID.getText().trim();
            String password = Password_ID.getText();
            String confirmPassword = Confirmpw_ID.getText();

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
                return;
            }
            if (fullName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Full name cannot be empty.");
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageText);
                if (age < 18 || age > 100) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Age must be between 18 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid age.");
                return;
            }
            if (userCollection.find(Filters.eq("Username", username)).first() != null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Username is already taken.");
                return;
            }

            if (password.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 6 characters long.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Passwords do not match.");
                return;
            }
            if (!Entertainment_ID.isSelected() && !Lifestyle_and_Culture_ID.isSelected() &&
                    !Politics_ID.isSelected() && !Science_ID.isSelected() &&
                    !Sport_ID.isSelected() && !Tech_ID.isSelected()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select at least one interest category.");
                return;
            }

            List<String> interests = new ArrayList<>();
            if (Entertainment_ID.isSelected()) interests.add("Entertainment");
            if (Lifestyle_and_Culture_ID.isSelected()) interests.add("Lifestyle and Culture");
            if (Politics_ID.isSelected()) interests.add("Politics");
            if (Science_ID.isSelected()) interests.add("Science");
            if (Sport_ID.isSelected()) interests.add("Sport");
            if (Tech_ID.isSelected()) interests.add("Tech");

            registerUser(username, password, email, fullName, age, interests);
        }
    }

    private void registerUser(String username, String password, String email, String fullName, int age, List<String> interests) {
        Document newUser = new Document("Username", username)
                .append("Password", password)
                .append("Email", email)
                .append("Full_Name", fullName)
                .append("Age", age)
                .append("Interests", interests);

        userCollection.insertOne(newUser);
        showAlert(Alert.AlertType.INFORMATION, "Registration", "User registered successfully.");

        // Clear input fields
        Username_ID.clear();
        Password_ID.clear();
        Confirmpw_ID.clear();
        Email_ID.clear();
        Full_name_ID.clear();
        Age_ID.clear();

        // Clear CheckBox selections
        Entertainment_ID.setSelected(false);
        Lifestyle_and_Culture_ID.setSelected(false);
        Politics_ID.setSelected(false);
        Science_ID.setSelected(false);
        Sport_ID.setSelected(false);
        Tech_ID.setSelected(false);

        // Bring main pane to the front
        mainpane.toFront();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("User_Detail");
        loginCollection = database.getCollection("User_login_Detail");
        adminCollection = database.getCollection("admin_details");
        data_sign_up.setOnAction(this::validateAndRegisterUser);
    }

    @FXML
    public void buttonClicksConfig(ActionEvent actionEvent) {
        if (actionEvent.getSource() == back_to_main) mainpane.toFront();
        if (actionEvent.getSource() == Sign_Up_page) Signup_menu.toFront();
    }

    @FXML
    private void handleCloseButtonClick(MouseEvent event) {
        // Close the application
        javafx.application.Platform.exit();
    }

}