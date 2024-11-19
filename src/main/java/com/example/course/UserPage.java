package com.example.course;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserPage implements Initializable {

    @FXML
    private Label Profile_Age;

    @FXML
    private Label Profile_Email;

    @FXML
    private Label Profile_Full_Name;

    @FXML
    private Label Profile_My_Preferences;

    @FXML
    private Label Profile_Username;

    @FXML
    private Button User_Categories;

    @FXML
    private Pane User_Categories_Face;

    @FXML
    private Button User_Home;

    @FXML
    private Pane User_Home_Face;

    @FXML
    private Button User_Log_Out;

    @FXML
    private AnchorPane User_Page;

    @FXML
    private Button User_Profile;

    @FXML
    private Pane User_Profile_Face;

    @FXML
    private Button User_Recommendations;

    @FXML
    private Pane User_Recommendations_Face;

    @FXML
    private Button User_Saved_Articals;

    @FXML
    private Pane User_Saved_Articals_Face;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private String currentUsername;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize MongoDB connection
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("User_Detail");
    }

    public void setUsername(String username) {
        this.currentUsername = username;
        loadUserProfile();
    }

    private void loadUserProfile() {
        Document user = userCollection.find(Filters.eq("Username", currentUsername)).first();

        if (user != null) {
            // Update the profile labels with user data
            Profile_Username.setText(user.getString("Username"));
            Profile_Full_Name.setText(user.getString("Full_Name"));
            Profile_Email.setText(user.getString("Email"));
            Profile_Age.setText(String.valueOf(user.getInteger("Age")));

            // Handle interests/preferences, showing one per line
            List<String> interests = (List<String>) user.get("Interests");
            if (interests != null && !interests.isEmpty()) {
                String preferencesText = String.join("\n", interests); // Use newline (\n) for line breaks
                Profile_My_Preferences.setText(preferencesText);
            } else {
                Profile_My_Preferences.setText("No preferences set.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load user profile data.");
        }
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
            // Refresh profile data when switching to profile view
            loadUserProfile();
        } else if (actionEvent.getSource() == User_Saved_Articals) {
            User_Saved_Articals_Face.toFront();
        }
    }

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