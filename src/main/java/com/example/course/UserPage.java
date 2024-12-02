package com.example.course;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserPage implements Initializable {

    @FXML
    private TextField Edit_Age_ID, Edit_Confirm_PW, Edit_Current_PW, Edit_Email_ID, Edit_Full_name_ID, Edit_New_PW, Edit_Username_ID;

    @FXML
    private CheckBox Edit_Entertainment_ID, Edit_Lifestyle_and_Culture_ID, Edit_Politics_ID, Edit_Science_ID, Edit_Sport_ID, Edit_Tech_ID;

    @FXML
    private Pane Edit_User_Profile_Page, User_Categories_Face, User_Home_Face, User_Profile_Face, User_Recommendations_Face, User_Saved_Articals_Face;

    @FXML
    private Label Profile_Age, Profile_Email, Profile_Full_Name, Profile_My_Preferences, Profile_Username;

    @FXML
    private Button Update_Profile, User_Categories, User_Home, User_Log_Out, User_Profile, User_Recommendations, User_Saved_Articals, User_edit_Profile;

    @FXML
    private TableColumn<Document, String> Home_artical_category;

    @FXML
    private TableView<Document> Home_artical_table;

    @FXML
    private TableColumn<Document, String> Home_artical_title;

    @FXML
    private AnchorPane User_Page;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private String currentUsername;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("User_Detail");

        initializeCheckboxes();
        loadartical();
    }

    private void initializeCheckboxes() {
        Edit_Entertainment_ID.setOnAction(event -> handleCheckboxClick(Edit_Entertainment_ID));
        Edit_Lifestyle_and_Culture_ID.setOnAction(event -> handleCheckboxClick(Edit_Lifestyle_and_Culture_ID));
        Edit_Politics_ID.setOnAction(event -> handleCheckboxClick(Edit_Politics_ID));
        Edit_Science_ID.setOnAction(event -> handleCheckboxClick(Edit_Science_ID));
        Edit_Sport_ID.setOnAction(event -> handleCheckboxClick(Edit_Sport_ID));
        Edit_Tech_ID.setOnAction(event -> handleCheckboxClick(Edit_Tech_ID));
    }

    private void handleCheckboxClick(CheckBox checkbox) {
        System.out.println(checkbox.getText() + " selected: " + checkbox.isSelected());
    }

    public void setUsername(String username) {
        this.currentUsername = username;
        loadUserProfile();
    }

    private void loadUserProfile() {
        Document user = userCollection.find(Filters.eq("Username", currentUsername)).first();

        if (user != null) {
            Profile_Username.setText(user.getString("Username"));
            Profile_Full_Name.setText(user.getString("Full_Name"));
            Profile_Email.setText(user.getString("Email"));
            Profile_Age.setText(String.valueOf(user.getInteger("Age")));

            List<String> interests = (List<String>) user.get("Interests");
            if (interests != null && !interests.isEmpty()) {
                String preferencesText = String.join("\n", interests);
                Profile_My_Preferences.setText(preferencesText);
            } else {
                Profile_My_Preferences.setText("No preferences set.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load user profile data.");
        }
    }

    @FXML
    private void loadEditProfile() {
        Document user = userCollection.find(Filters.eq("Username", currentUsername)).first();

        if (user != null) {
            clearEditProfileForm();

            Edit_Username_ID.setText(user.getString("Username"));
            Edit_Full_name_ID.setText(user.getString("Full_Name"));
            Edit_Email_ID.setText(user.getString("Email"));
            Edit_Age_ID.setText(String.valueOf(user.getInteger("Age")));

            List<String> interests = (List<String>) user.get("Interests");
            if (interests != null) {
                Edit_Entertainment_ID.setSelected(interests.contains("Entertainment"));
                Edit_Lifestyle_and_Culture_ID.setSelected(interests.contains("Lifestyle and Culture"));
                Edit_Politics_ID.setSelected(interests.contains("Politics"));
                Edit_Science_ID.setSelected(interests.contains("Science"));
                Edit_Sport_ID.setSelected(interests.contains("Sport"));
                Edit_Tech_ID.setSelected(interests.contains("Tech"));
            }

            enableCheckboxes(true);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load user details.");
        }
    }

    private void clearEditProfileForm() {
        Edit_Username_ID.clear();
        Edit_Full_name_ID.clear();
        Edit_Email_ID.clear();
        Edit_Age_ID.clear();
        Edit_Current_PW.clear();
        Edit_New_PW.clear();
        Edit_Confirm_PW.clear();

        Edit_Entertainment_ID.setSelected(false);
        Edit_Lifestyle_and_Culture_ID.setSelected(false);
        Edit_Politics_ID.setSelected(false);
        Edit_Science_ID.setSelected(false);
        Edit_Sport_ID.setSelected(false);
        Edit_Tech_ID.setSelected(false);
    }

    private void enableCheckboxes(boolean enable) {
        Edit_Entertainment_ID.setDisable(!enable);
        Edit_Lifestyle_and_Culture_ID.setDisable(!enable);
        Edit_Politics_ID.setDisable(!enable);
        Edit_Science_ID.setDisable(!enable);
        Edit_Sport_ID.setDisable(!enable);
        Edit_Tech_ID.setDisable(!enable);
    }

    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        try {
            // Validate input fields
            String currentPassword = Edit_Current_PW.getText().trim();
            String newPassword = Edit_New_PW.getText().trim();
            String confirmPassword = Edit_Confirm_PW.getText().trim();
            String updatedUsername = Edit_Username_ID.getText().trim();
            String updatedFullName = Edit_Full_name_ID.getText().trim();
            String updatedEmail = Edit_Email_ID.getText().trim();
            String updatedAgeText = Edit_Age_ID.getText().trim();

            // Check current user password
            Document user = userCollection.find(Filters.eq("Username", currentUsername)).first();
            if (user == null || !user.getString("Password").equals(currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "The current password is incorrect.");
                return;
            }

            // Validate new password if provided
            if (!newPassword.isEmpty()) {
                if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*")) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "New password must be at least 8 characters long, include uppercase, lowercase, and a number.");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "New password and confirmation do not match.");
                    return;
                }
            }

            // Validate email
            if (!updatedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
                return;
            }

            // Validate age
            int updatedAge;
            try {
                updatedAge = Integer.parseInt(updatedAgeText);
                if (updatedAge < 15 || updatedAge > 100) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Age must be between 15 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid age.");
                return;
            }

            // Collect updated interests
            List<String> updatedInterests = new ArrayList<>();
            if (Edit_Entertainment_ID.isSelected()) updatedInterests.add("Entertainment");
            if (Edit_Lifestyle_and_Culture_ID.isSelected()) updatedInterests.add("Lifestyle and Culture");
            if (Edit_Politics_ID.isSelected()) updatedInterests.add("Politics");
            if (Edit_Science_ID.isSelected()) updatedInterests.add("Science");
            if (Edit_Sport_ID.isSelected()) updatedInterests.add("Sport");
            if (Edit_Tech_ID.isSelected()) updatedInterests.add("Tech");

            // Create update document
            Document updateDoc = new Document()
                    .append("Username", updatedUsername)
                    .append("Full_Name", updatedFullName)
                    .append("Email", updatedEmail)
                    .append("Age", updatedAge)
                    .append("Interests", updatedInterests);

            // Only update password if a new one is provided
            if (!newPassword.isEmpty()) {
                updateDoc.append("Password", newPassword);
            }

            // Perform the update in MongoDB
            userCollection.updateOne(
                    Filters.eq("Username", currentUsername),
                    new Document("$set", updateDoc)
            );

            currentUsername = updatedUsername; // Update current username if it changes
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile has been updated successfully.");
            loadUserProfile(); // Refresh the profile view
            User_Profile_Face.toFront(); // Switch to the profile pane
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating your profile.");
        }
    }


    @FXML
    private void showEditProfilePane(ActionEvent event) {
        Edit_User_Profile_Page.toFront();
        loadEditProfile();
    }

    @FXML
    private void UserbuttonClicksConfig(ActionEvent actionEvent) {
        if (actionEvent.getSource() == User_Home) {
            User_Home_Face.toFront();
        } else if (actionEvent.getSource() == User_Categories) {
            User_Categories_Face.toFront();
        } else if (actionEvent.getSource() == User_Recommendations) {
            User_Recommendations_Face.toFront();
        } else if (actionEvent.getSource() == User_Profile) {
            User_Profile_Face.toFront();
            loadUserProfile();
        } else if (actionEvent.getSource() == User_Saved_Articals) {
            User_Saved_Articals_Face.toFront();
        } else if (actionEvent.getSource() == User_edit_Profile) {
            showEditProfilePane(actionEvent);
        }
    }

    @FXML
    private void loadartical(){
        MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
        MongoCollection<Document> collection = database.getCollection("my_articals");

        List<Document> articals = collection.find().into(new ArrayList<>());
        ObservableList<Document> data = FXCollections.observableArrayList(articals);

        Home_artical_title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("title")));
        Home_artical_category.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("content")));
        Home_artical_table.setItems(data);
    }

    @FXML
    private void viewButtonClicked(){
        Document selectedArticale = Home_artical_table.getSelectionModel().getSelectedItem();
        if (selectedArticale != null) {
            String title = selectedArticale.getString("title");
            String content = selectedArticale.getString("content");
            String category = selectedArticale.getString("category");

            try{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("article.fxml"));
                Parent root = loader.load();

                ArticleView articleView = loader.getController();

                articleView.displayContent(title, content ,category);

                Stage stage = new Stage();
                stage.setTitle("Article View");
                stage.setScene(new Scene(root));
                stage.show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    static String username = appcontroller.Username;



    @FXML
    private void handleUserLogOut(ActionEvent event) {
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
