package com.example.course;

import com.mongodb.client.model.Filters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class appcontroller implements Initializable {

    // FXML Components
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

    // Database Instance
    private Database database;
    public static String Username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        database = new Database("mongodb+srv://praveen:praveen2003@cluster0.dsqsv.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0", "News_Recommendation");
    }

    @FXML
    private void control_signin(ActionEvent event) {
        String username = Username_text.getText();
        String password = password_text.getText();

        Member member = database.getMember(username);

        if (member != null && member.authenticate(password)) {
            // Set the global Username variable
            Username = username;

            // Save login details to the database
            database.saveLoginDetails(username);

            // Navigate to the appropriate page
            if (member instanceof Admin) {
                showAlert(Alert.AlertType.INFORMATION, "Login", "Welcome admin " + username);
                goToAdminPage();
            } else if (member instanceof User) {
                showAlert(Alert.AlertType.INFORMATION, "Login", "Welcome " + username);
                goToUserPage();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login", "Incorrect username or password");
        }
    }

    @FXML
    private void validateAndRegisterUser() {
        String email = Email_ID.getText().trim();
        String fullName = Full_name_ID.getText().trim();
        String ageText = Age_ID.getText().trim();
        String username = Username_ID.getText().trim();
        String password = Password_ID.getText();
        String confirmPassword = Confirmpw_ID.getText();

        // Validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return;
        }
        if (database.getMember(username) != null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username or email is already registered.");
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
        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username cannot be empty.");
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
        if (!atLeastOneCategorySelected()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select at least one interest category.");
            return;
        }

        List<String> interests = collectSelectedCategories();
        User user = new User(username, password, email, fullName, age, interests);

        database.registerUser(user);
        showAlert(Alert.AlertType.INFORMATION, "Registration", "User registered successfully.");
        clearInputFields();
    }

    private boolean atLeastOneCategorySelected() {
        return Entertainment_ID.isSelected() || Lifestyle_and_Culture_ID.isSelected() ||
                Politics_ID.isSelected() || Science_ID.isSelected() ||
                Sport_ID.isSelected() || Tech_ID.isSelected();
    }

    private List<String> collectSelectedCategories() {
        List<String> selectedInterests = new ArrayList<>();
        if (Entertainment_ID.isSelected()) selectedInterests.add("Entertainment");
        if (Lifestyle_and_Culture_ID.isSelected()) selectedInterests.add("Lifestyle and Culture");
        if (Politics_ID.isSelected()) selectedInterests.add("Politics");
        if (Science_ID.isSelected()) selectedInterests.add("Science");
        if (Sport_ID.isSelected()) selectedInterests.add("Sport");
        if (Tech_ID.isSelected()) selectedInterests.add("Tech");
        return selectedInterests;
    }

    private void goToAdminPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-page.fxml"));
            Parent adminPageRoot = loader.load();

            AdminPage adminPageController = loader.getController();
            adminPageController.setAdminUsername(Username);

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

            // Get the UserPage controller and pass the username
            UserPage userPageController = loader.getController();
            userPageController.setUsername(Username); // Call the correct method to set the username

            // Load the scene
            Scene userScene = new Scene(userPageRoot);
            Stage stage = (Stage) Username_text.getScene().getWindow();
            stage.setScene(userScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the user page.");
        }
    }


    private void clearInputFields() {
        Username_ID.clear();
        Password_ID.clear();
        Confirmpw_ID.clear();
        Email_ID.clear();
        Full_name_ID.clear();
        Age_ID.clear();

        Entertainment_ID.setSelected(false);
        Lifestyle_and_Culture_ID.setSelected(false);
        Politics_ID.setSelected(false);
        Science_ID.setSelected(false);
        Sport_ID.setSelected(false);
        Tech_ID.setSelected(false);

        mainpane.toFront();
    }

    @FXML
    public void buttonClicksConfig(ActionEvent actionEvent) {
        if (actionEvent.getSource() == back_to_main) {
            mainpane.toFront();
        } else if (actionEvent.getSource() == Sign_Up_page) {
            Signup_menu.toFront();
        } else if (actionEvent.getSource() == data_sign_up) {
            validateAndRegisterUser();
        }
    }

    @FXML
    private void handleCloseButtonClick(MouseEvent event) {
        javafx.application.Platform.exit();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
