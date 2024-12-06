package com.example.course;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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
    private Pane Edit_User_Profile_Page, User_Home_Face, User_Profile_Face, User_Recommendations_Face, User_Saved_Articals_Face;

    @FXML
    private Label Profile_Age, Profile_Email, Profile_Full_Name, Profile_My_Preferences, Profile_Username;

    @FXML
    private Button  User_Home, User_Profile, User_Recommendations, User_Saved_Articals, User_edit_Profile;

    @FXML
    private TableColumn<Document, String> Home_artical_category;

    @FXML
    private TableView<Document> Home_artical_table;

    @FXML
    private TableColumn<Document, String> Home_artical_title;

    @FXML
    private TableColumn<Document, String> save_article_category;

    @FXML
    private TableView<Document> save_article_table;

    @FXML
    private TableColumn<Document, String> save_article_title;

    @FXML
    private TableColumn<Document, String> Recommend_Artical_Title;

    @FXML
    private TableView<Document> Recommend_Artical_table;

    @FXML
    private TableColumn<Document, String> Recommend_Artical_Category;


    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> userCollection;
    private String currentUsername;

    static String username = appcontroller.Username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mongoClient = MongoClients.create("mongodb+srv://praveen:praveen2003@cluster0.dsqsv.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("User_Detail");

        // Setup profile editing TextField
        Edit_Username_ID.setEditable(false);
        Edit_Username_ID.setStyle("-fx-background-color: transparent;");

        initializeCheckboxes();
        loadartical();
        savedNews();
        NewsRecommend();
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
        String category = checkbox.getText();
        boolean isSelected = checkbox.isSelected();

        // Update the points in the Articlepoints collection
        MongoCollection<Document> articlePointsCollection = database.getCollection("Articlepoints");
        int pointsChange = isSelected ? 5 : -5;

        articlePointsCollection.updateOne(
                Filters.eq("Username", currentUsername),
                Updates.inc(category, pointsChange)
        );

        System.out.println(category + " selected: " + isSelected + ". Points updated by " + pointsChange);
    }


    public void setUsername(String username) {
        this.currentUsername = username;
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Current username is not set.");
            return;
        }

        try {
            Document user = userCollection.find(Filters.eq("Username", currentUsername)).first();

            if (user != null) {
                Profile_Username.setText(user.getString("Username"));
                Profile_Full_Name.setText(user.getString("Full_Name"));
                Profile_Email.setText(user.getString("Email"));

                Integer age = user.getInteger("Age");
                Profile_Age.setText(age != null ? String.valueOf(age) : "N/A");

                List<String> interests = user.getList("Interests", String.class);
                if (interests != null && !interests.isEmpty()) {
                    Profile_My_Preferences.setText(String.join("\n", interests));
                } else {
                    Profile_My_Preferences.setText("No preferences set.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find user profile for username: " + currentUsername);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while loading the user profile: " + e.getMessage());
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
        Home_artical_category.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("category")));
        Home_artical_table.setItems(data);
    }

    private void handleViewAction(TableView<Document> tableView) {
        Document selectedArticle = tableView.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            String title = selectedArticle.getString("title");
            String content = selectedArticle.getString("content");
            String category = selectedArticle.getString("category");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("article.fxml"));
                Parent root = loader.load();

                ArticleView articleView = loader.getController();
                articleView.displayContent(title, content, category);

                articalPointUpdate(category);

                Stage stage = new Stage();
                stage.setTitle("Article View");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to load the article view.");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select an article to view.");
        }
    }

    @FXML
    private void handleHomeTableView() {
        handleViewAction(Home_artical_table);
    }

    @FXML
    private void handleRecommendTableView() {
        handleViewAction(Recommend_Artical_table);
    }

    @FXML
    private void handleSavedTableView() {
        handleViewAction(save_article_table);
    }

    @FXML
    private void handleUnsaveAction(ActionEvent event) {
        // Get the selected article from the table
        Document selectedArticle = save_article_table.getSelectionModel().getSelectedItem();

        if (selectedArticle != null) {
            String selectedTitle = selectedArticle.getString("title");

            // Access the necessary collections
            MongoCollection<Document> saveCollection = database.getCollection("Saved_articles");

            // Find the saved articles for the logged-in user
            Document userSaves = saveCollection.find(Filters.eq("username", username)).first();

            if (userSaves != null && userSaves.containsKey("Saved")) {
                List<String> savedTitles = userSaves.getList("Saved", String.class);

                // Remove the selected article's title from the list
                savedTitles.remove(selectedTitle);

                // Update the MongoDB document with the modified list
                saveCollection.updateOne(
                        Filters.eq("username", username),
                        Updates.set("Saved", savedTitles)
                );

                // Refresh the saved articles table
                savedNews();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Article unsaved successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No saved articles found for the user.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an article to unsave.");
        }
    }

    private void articalPointUpdate(String category){
        MongoCollection<Document> userArticalcollection =  database.getCollection("Articlepoints");

        userArticalcollection.updateOne(
          Filters.eq("Username", appcontroller.Username),
                Updates.inc(category, 1)
        );
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
    @FXML
    public void NewsRecommend() {
        MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
        MongoCollection<Document> ArticleCollection = database.getCollection("my_articals");
        MongoCollection<Document> userArticlesPointCollection = database.getCollection("Articlepoints");

        String currentUsername = appcontroller.Username;

        Document acquirePoints = userArticlesPointCollection.find(Filters.eq("Username", currentUsername)).first();

        if (acquirePoints == null) {
            System.out.println("User data not found.");
            return;
        }

        // Collect categories with points ≥ 5 and their respective points
        List<String> favoriteCategories = new ArrayList<>();
        List<Integer> categoryPoints = new ArrayList<>();
        int totalPoints = 0;

        for (String category : acquirePoints.keySet()) {
            if (!category.equals("_id") && !category.equals("Username")) {
                int points = acquirePoints.getInteger(category, 0);
                if (points >= 5) {
                    favoriteCategories.add(category);
                    categoryPoints.add(points);
                    totalPoints += points;
                }
            }
        }

        if (favoriteCategories.isEmpty()) {
            System.out.println("No preferred categories with points > 5.");
            return;
        }

        // Allocate a number of articles to each category based on its points
        int totalArticlesToDisplay = 15;
        List<Document> finalArticles = new ArrayList<>();

        for (int i = 0; i < favoriteCategories.size(); i++) {
            String category = favoriteCategories.get(i);
            int categoryShare = (categoryPoints.get(i) * totalArticlesToDisplay) / totalPoints;
            categoryShare = Math.max(categoryShare, 1); // Ensure at least 1 article per category if possible

            // Fetch articles for this category, limited by the calculated share
            List<Document> categoryArticles = ArticleCollection
                    .find(Filters.eq("category", category))
                    .limit(categoryShare)
                    .into(new ArrayList<>());

            finalArticles.addAll(categoryArticles);
        }

        // If we have more articles than required, trim the list
        if (finalArticles.size() > totalArticlesToDisplay) {
            finalArticles = finalArticles.subList(0, totalArticlesToDisplay);
        }

        // Bind the data to the TableView
        ObservableList<Document> articleData = FXCollections.observableArrayList(finalArticles);

        Recommend_Artical_Category.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("category")));
        Recommend_Artical_Title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("title")));

        Recommend_Artical_table.setItems(articleData);
    }


    @FXML
    private void savedNews() {
        // Access the necessary collections
        MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
        MongoCollection<Document> articlesCollection = database.getCollection("my_articals");
        MongoCollection<Document> saveCollection = database.getCollection("Saved_articles");

        // Find the saved articles for the logged-in user
        Document userSaves = saveCollection.find(Filters.eq("username", username)).first();

        if (userSaves != null && userSaves.containsKey("Saved")) {
            // Retrieve the saved article titles
            List<String> savedTitles = userSaves.getList("Saved", String.class);

            // Find the articles in the articles collection that match the saved titles
            List<Document> savedArticles = articlesCollection.find(Filters.in("title", savedTitles)).into(new ArrayList<>());

            // Bind the data to the TableView
            ObservableList<Document> userData = FXCollections.observableArrayList(savedArticles);

            save_article_category.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("category")));
            save_article_title.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getString("title")));

            save_article_table.setItems(userData);
        } else {
            // If no saved articles, clear the table and notify the user
            save_article_table.setItems(FXCollections.observableArrayList());
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
