package com.example.course;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPage implements Initializable {

    @FXML
    private TextArea Admin_Artical_Content;

    @FXML
    private TextField Admin_Artical_Title;

    @FXML
    private Label Admin_Profile_Age;

    @FXML
    private Label Admin_Profile_Email;

    @FXML
    private Label Admin_Profile_Full_Name;

    @FXML
    private Label Admin_Profile_Username;

    @FXML
    private Button Admin_add_Artical;

    @FXML
    private Button Admin_delete_Artical;

    @FXML
    private Button Admin_edit_Profile;

    @FXML
    private AnchorPane Admin_page;

    @FXML
    private TableColumn<User, String> Age_Pane;

    @FXML
    private Pane Edit_Admin_Profile_Page;

    @FXML
    private TextField Edit_Age_ID;

    @FXML
    private TextField Edit_Confirm_PW;

    @FXML
    private TextField Edit_Current_PW;

    @FXML
    private TextField Edit_Email_ID;

    @FXML
    private TextField Edit_Full_name_ID;

    @FXML
    private TextField Edit_New_PW;

    @FXML
    private TextField Edit_Username_ID;

    @FXML
    private TableColumn<User, String> Email_Pane;

    @FXML
    private TableColumn<User, String> Full_Name_Pane;

    @FXML
    private TableColumn<User, String> Preferences_Pane;

    @FXML
    private Button Update_Admin_Profile;

    @FXML
    private TableColumn<User, String> Username_Pane;

    @FXML
    private Button admin_Log_Out;

    @FXML
    private Button admin_Manage_User;

    @FXML
    private Button admin_Manage_artical;

    @FXML
    private Button admin_Profile;

    @FXML
    private Pane admin_Profile_Face;

    @FXML
    private Button admin_User_Interface;

    @FXML
    private Pane admin_add_artical_face;

    @FXML
    private Pane admin_manage_artical_face;

    @FXML
    private Pane admin_manage_user_face;

    @FXML
    private Pane admin_user_interface_face;

    @FXML
    private TableView<User> table_User;


    private String adminUsername;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> adminCollection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize MongoDB connection
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("News_Recommendation");
        adminCollection = database.getCollection("admin_details");

        // Bind columns to User properties
        Username_Pane.setCellValueFactory(new PropertyValueFactory<>("username"));
        Full_Name_Pane.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        Email_Pane.setCellValueFactory(new PropertyValueFactory<>("email"));
        Age_Pane.setCellValueFactory(new PropertyValueFactory<>("age"));
        Preferences_Pane.setCellValueFactory(new PropertyValueFactory<>("interests"));
    }

    public void setAdminUsername(String username) {
        this.adminUsername = username;
        loadAdminProfile(); // Load profile data when username is set
    }

    @FXML
    private void handleButtonClicks(ActionEvent event) {
        Object source = event.getSource();

        if (source == admin_Profile) {
            admin_Profile_Face.toFront();
            loadAdminProfile();
        } else if (source == admin_Manage_User) {
            loadUserDetails();
            admin_manage_user_face.toFront();
        } else if (source == admin_Manage_artical) {
            admin_manage_artical_face.toFront();
        } else if (source == admin_User_Interface) {
            admin_user_interface_face.toFront();
        } else if (source == Admin_add_Artical) {
            admin_add_artical_face.toFront();
        } else if (source == admin_Log_Out) {
            handleAdminLogOut(event);
        } else if (source == Admin_edit_Profile) {
            // Show the Edit Admin Profile Page and load data into TextFields
            Edit_Admin_Profile_Page.toFront();
            loadAdminProfileForEdit();
        }
    }

    @FXML
    private void loadUserDetails() {
        MongoCollection<Document> userCollection = database.getCollection("User_Detail"); // Adjust collection name
        ObservableList<User> users = FXCollections.observableArrayList();

        for (Document doc : userCollection.find()) {
            String username = doc.getString("Username");
            String fullName = doc.getString("Full_Name");
            String email = doc.getString("Email");
            int age = doc.getInteger("Age", 0);
            List<String> interests = doc.getList("Interests", String.class);
            String interestsString = String.join(", ", interests);

            users.add(new User(username, fullName, email, age, interestsString));
        }

        table_User.setItems(users);
    }


    private void loadAdminProfileForEdit() {
        if (adminUsername != null && !adminUsername.isEmpty()) {
            Document adminDoc = adminCollection.find(Filters.eq("Username", adminUsername)).first();

            if (adminDoc != null) {
                // Populate the TextFields with admin data
                Edit_Username_ID.setText(adminDoc.getString("Username"));
                Edit_Full_name_ID.setText(adminDoc.getString("Full_Name"));
                Edit_Email_ID.setText(adminDoc.getString("Email"));
                Edit_Age_ID.setText(String.valueOf(adminDoc.getInteger("Age", 0)));
            } else {
                showAlert(Alert.AlertType.ERROR, "Profile Error", "Could not load admin profile");
            }
        }
    }

    @FXML
    private void handleUpdateAdminProfile(ActionEvent event) {
        // Get values from input fields
        String username = Edit_Username_ID.getText().trim();
        String fullName = Edit_Full_name_ID.getText().trim();
        String ageText = Edit_Age_ID.getText().trim();
        String email = Edit_Email_ID.getText().trim();
        String currentPW = Edit_Current_PW.getText().trim();
        String newPW = Edit_New_PW.getText().trim();
        String confirmPW = Edit_Confirm_PW.getText().trim();

        // Validate inputs
        if (username.isEmpty() || fullName.isEmpty() || ageText.isEmpty() || email.isEmpty() || currentPW.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields except new password must be filled.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Age must be a valid positive integer.");
            return;
        }

        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid email format.");
            return;
        }

        // Check if the new password is entered and matches confirmation
        if (!newPW.isEmpty() && !newPW.equals(confirmPW)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "New password and confirmation password must match.");
            return;
        }

        // Fetch admin record from the database
        Document adminDoc = adminCollection.find(Filters.eq("Username", adminUsername)).first();

        if (adminDoc == null) {
            showAlert(Alert.AlertType.ERROR, "Profile Error", "Admin profile not found.");
            return;
        }

        String storedPassword = adminDoc.getString("Password");
        if (!storedPassword.equals(currentPW)) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", "Current password is incorrect.");
            return;
        }

        // Create a document with updated fields
        Document updatedDoc = new Document()
                .append("Username", username)
                .append("Full_Name", fullName)
                .append("Age", age)
                .append("Email", email);

        // Update the password if a new one is provided
        if (!newPW.isEmpty()) {
            updatedDoc.append("Password", newPW);
        } else {
            updatedDoc.append("Password", storedPassword); // Retain existing password
        }

        // Perform update in the database
        adminCollection.updateOne(Filters.eq("Username", adminUsername), new Document("$set", updatedDoc));
        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully.");

        // Reload profile to reflect changes
        setAdminUsername(username); // Update username if changed
    }


    private void loadAdminProfile() {
        if (adminUsername != null && !adminUsername.isEmpty()) {
            Document adminDoc = adminCollection.find(Filters.eq("Username", adminUsername)).first();

            if (adminDoc != null) {
                // Update the labels with admin information
                Admin_Profile_Username.setText(adminDoc.getString("Username"));
                Admin_Profile_Full_Name.setText(adminDoc.getString("Full_Name"));
                Admin_Profile_Email.setText(adminDoc.getString("Email"));
                Admin_Profile_Age.setText(String.valueOf(adminDoc.getInteger("Age", 0)));
            } else {
                showAlert(Alert.AlertType.ERROR, "Profile Error", "Could not load admin profile");
            }
        }
    }

    @FXML
    private void handleAdminLogOut(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent userPageRoot = loader.load();
            Scene scene = new Scene(userPageRoot);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load the login page.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}