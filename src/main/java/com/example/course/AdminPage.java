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
import org.bson.json.JsonObject;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
    private TableColumn<Document, String> Email_Pane;

    @FXML
    private TableColumn<Document, String> Full_Name_Pane;

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
    private Pane delete_artical_page;

    @FXML
    private TableView<Article> delete_artical_table;

    @FXML
    private TableColumn<Article, String> Artical_Title_Column;

    @FXML
    private TableColumn<Article, String> Artical_Content_Column;

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

    @FXML
    private Button add_artical_button;


    private String adminUsername;
    private final Database database;

    // Constructor for dependency injection
    public AdminPage() {
        this.database = new Database("mongodb+srv://praveen:praveen2003@cluster0.dsqsv.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0", "News_Recommendation");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Set the text for the TextField
        Edit_Username_ID.setText("Username");

        // Disable editing ability
        Edit_Username_ID.setEditable(false);
        Edit_Username_ID.setStyle("-fx-background-color: transparent;"); // makes the background look like a label


        Username_Pane.setCellValueFactory(new PropertyValueFactory<>("username"));
        Full_Name_Pane.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        Email_Pane.setCellValueFactory(new PropertyValueFactory<>("email"));
        Age_Pane.setCellValueFactory(new PropertyValueFactory<>("age")); // Matches getter
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
        } else if (source == Admin_delete_Artical) {
            delete_artical_page.toFront(); // Bring the delete article page to the front
            loadArticles(); // Populate the table with articles
        }
    }

    @FXML
    private void handleAddArticle(ActionEvent event) {
        // Collect data from input fields
        String articleTitle = Admin_Artical_Title.getText().trim();
        String articleContent = Admin_Artical_Content.getText().trim();

        // Validate input fields
        if (articleTitle.isEmpty() || articleContent.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title and Content must not be empty.");
            return;
        }
        if (articleTitle.length() < 5 || articleTitle.length() > 100) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be between 5 and 100 characters.");
            return;
        }
        if (articleContent.length() < 20) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Content must be at least 20 characters.");
            return;
        }

        // Create Article object
        Article article = new Article(articleTitle, articleContent);

        // Use Database method to add article
        database.addArticle(article);

        // Send data to Flask backend
        try {
            URL url = new URL("http://127.0.0.1:5000/upload_article");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // Prepare JSON data
            String jsonData = String.format("{\"title\": \"%s\", \"content\": \"%s\"}", articleTitle, articleContent);

            // Write JSON data to the request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Article added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Server Error", "Failed to add article. Please try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not connect to the server.");
        }
    }


    @FXML
    private void loadUserDetails() {
        List<User> users = database.getAllUsers();
        table_User.setItems(FXCollections.observableArrayList(users));
    }

    private void loadAdminProfileForEdit() {
        if (adminUsername != null && !adminUsername.isEmpty()) {
            Member admin = database.getMember(adminUsername);

            if (admin != null) {
                // Populate the TextFields with admin data
                Edit_Username_ID.setText(admin.getUsername());
                Edit_Full_name_ID.setText(admin.getFullName());
                Edit_Email_ID.setText(admin.getEmail());
                Edit_Age_ID.setText(String.valueOf(admin.getAge()));
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
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Current password must be filled.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 15 || age > 100) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Age must be between 15 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid age.");
            return;
        }


        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid email format.");
            return;
        }

        // Validate new password if provided
        if (!newPW.isEmpty()) {
            if (newPW.length() < 8 || !newPW.matches(".*[A-Z].*") || !newPW.matches(".*[a-z].*") || !newPW.matches(".*\\d.*")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "New password must be at least 8 characters long, include uppercase, lowercase, and a number.");
                return;
            }
            if (!newPW.equals(confirmPW)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "New password and confirmation do not match.");
                return;
            }
        }

        // Fetch admin record from the database
        Document adminDoc = database.getAdminCollection().find(Filters.eq("Username", adminUsername)).first();

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
        database.getAdminCollection().updateOne(Filters.eq("Username", adminUsername), new Document("$set", updatedDoc));
        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully.");

        // Reload profile to reflect changes
        setAdminUsername(username); // Update username if changed
    }
    private void loadAdminProfile() {
        if (adminUsername != null && !adminUsername.isEmpty()) {
            Member admin = database.getMember(adminUsername);

            if (admin != null) {
                // Update labels with admin information
                Admin_Profile_Username.setText(admin.getUsername());
                Admin_Profile_Full_Name.setText(admin.getFullName());
                Admin_Profile_Email.setText(admin.getEmail());
                Admin_Profile_Age.setText(String.valueOf(admin.getAge())); // Set the age label
            } else {
                showAlert(Alert.AlertType.ERROR, "Profile Error", "Could not load admin profile");
            }
        }
    }


    @FXML
    private void loadArticles() {
        List<Article> articles = database.getAllArticles();

        // Bind the data to the table
        Artical_Title_Column.setCellValueFactory(new PropertyValueFactory<>("title"));
        Artical_Content_Column.setCellValueFactory(new PropertyValueFactory<>("content"));
        delete_artical_table.setItems(FXCollections.observableArrayList(articles));
    }

    @FXML
    private void handleDeleteArticle(ActionEvent event) {
        Article selectedArticle = delete_artical_table.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            // Use Database method to delete article
            database.deleteArticle(selectedArticle.getTitle());

            // Refresh the table
            loadArticles();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Article deleted successfully!");
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an article to delete.");
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = table_User.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            // Confirm deletion with the user
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete Confirmation");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Are you sure you want to delete the user: " + selectedUser.getUsername() + "?");

            if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
                // Attempt to delete the user from the database
                boolean isDeleted = database.deleteUser(selectedUser.getUsername());

                if (isDeleted) {
                    // Remove the user from the table
                    table_User.getItems().remove(selectedUser);

                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully!");
                } else {
                    // Show error message if deletion fails
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the user. Please try again.");
                }
            }
        } else {
            // Show warning if no user is selected
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
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