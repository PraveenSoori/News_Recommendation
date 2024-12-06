package com.example.course;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ArticleView implements Initializable {

    @FXML
    private Button dislike_btn;

    @FXML
    private Button like_btn;

    @FXML
    private Button rate_btn;

    @FXML
    private ComboBox<String> rate_combobox;

    @FXML
    private Button save_btn;

    @FXML
    private WebView webView;

    private String category;
    private String articleTitle;

    private final String Username = UserPage.username; // Assuming this is set correctly elsewhere.
    private final MongoDatabase database = MongoClients.create("mongodb+srv://praveen:praveen2003@cluster0.dsqsv.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0").getDatabase("News_Recommendation");

    public void displayContent(String title, String content, String category) {
        try {
            this.category = category;
            this.articleTitle = title;
            File file = new File("src/main/resources/com/example/course/web.html");
            String html = new String(Files.readAllBytes(file.toPath()));

            html = html.replace("<!-- title -->", title);
            html = html.replace("<!--content -->", content);

            WebEngine web = webView.getEngine();
            web.loadContent(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasAlreadyInteracted(String interactionType, String articleTitle) {
        try {
            MongoCollection<Document> saveArticleCollection = database.getCollection("Saved_articles");
            Document userArticleDoc = saveArticleCollection.find(Filters.eq("username", Username)).first();

            if (userArticleDoc != null && userArticleDoc.containsKey(interactionType)) {
                ArrayList<String> interactionList = (ArrayList<String>) userArticleDoc.get(interactionType);
                return interactionList.contains(articleTitle);
            }
        } catch (Exception e) {
            System.err.println("Failed to check interaction: " + e.getMessage());
        }
        return false;
    }


    @FXML
    private void buttonClickedOnRate() {
        if (!validateArticleAndCategory()) return;

        String selectedRating = rate_combobox.getSelectionModel().getSelectedItem();
        if (selectedRating == null) {
            showAlert("Rating Action", "Please select a rating before submitting.", Alert.AlertType.WARNING);
            return;
        }

        if (hasAlreadyInteracted("Rated", articleTitle)) {
            showAlert("Rating Action", "You have already rated this article.", Alert.AlertType.INFORMATION);
            return;
        }

        int points = pointsCalculate(selectedRating);
        if (points > 0) {
            CategoryUpdatePoints(points);
            articleInteractionSave("Rated", articleTitle);
            rate_combobox.setDisable(true);
            rate_btn.setDisable(true);
            showAlert("Rating Action", "You have rated the article as " + selectedRating + ".", Alert.AlertType.INFORMATION);
        }
    }



    private int pointsCalculate(String rating) {
        return switch (rating) {
            case "Not Bad" -> 1;
            case "Good" -> 2;
            case "Very Good" -> 3;
            case "Excellent" -> 4;
            case "Outstanding" -> 5;
            default -> 0;
        };
    }

    @FXML
    private void onLikeButtonAction() {
        if (validateArticleAndCategory()) {
            if (hasAlreadyInteracted("Liked", articleTitle)) {
                showAlert("Like Action", "You have already liked this article.", Alert.AlertType.INFORMATION);
            } else {
                CategoryUpdatePoints(2);
                articleInteractionSave("Liked", articleTitle);
                like_btn.setDisable(true);
                dislike_btn.setDisable(true);
                showAlert("Like Action", "You have liked the article successfully.", Alert.AlertType.INFORMATION);
            }
        }
    }


    @FXML
    private void onDisLikeButtonAction() {
        if (validateArticleAndCategory()) {
            if (hasAlreadyInteracted("Disliked", articleTitle)) {
                showAlert("Dislike Action", "You have already disliked this article.", Alert.AlertType.INFORMATION);
            } else {
                CategoryUpdatePoints(-2);
                articleInteractionSave("Disliked", articleTitle);
                like_btn.setDisable(true);
                dislike_btn.setDisable(true);
                showAlert("Dislike Action", "You have disliked the article successfully.", Alert.AlertType.INFORMATION);
            }
        }
    }



    private boolean validateArticleAndCategory() {
        if (articleTitle == null || articleTitle.isEmpty()) {
            System.out.println("Article title is not set. Please ensure the title is selected.");
            return false;
        }
        if (category == null || category.isEmpty()) {
            System.out.println("Category is not set. Please ensure the category is selected.");
            return false;
        }
        return true;
    }

    private void CategoryUpdatePoints(int points) {
        if (!validateArticleAndCategory()) return;

        try {
            MongoCollection<Document> articleCollection = database.getCollection("Articlepoints");

            // Ensure the document exists and initialize it if needed
            Document userDoc = articleCollection.find(Filters.eq("Username", Username)).first(); // Corrected field name to "Username"
            if (userDoc == null) {
                System.out.println("No document found for user. Initializing...");
                userDoc = new Document("Username", Username).append(category, 0); // Initialize category points
                articleCollection.insertOne(userDoc);
            } else if (!userDoc.containsKey(category)) {
                System.out.println("Category not found for user. Initializing category points...");
                articleCollection.updateOne(
                        Filters.eq("Username", Username), // Corrected field name to "Username"
                        Updates.set(category, 0) // Initialize category points
                );
            }

            // Perform the points update
            articleCollection.updateOne(
                    Filters.eq("Username", Username), // Corrected field name to "Username"
                    Updates.inc(category, points)
            );
            System.out.println("Updated " + category + " for user " + Username + " by " + points + " points.");
        } catch (Exception e) {
            System.err.println("Failed to update points for category: " + e.getMessage());
        }
    }
    private void articleInteractionSave(String interactionType, String articleTitle) {
        if (hasAlreadyInteracted(interactionType, articleTitle)) {
            System.out.println("User has already " + interactionType + " the article: " + articleTitle);
            return; // Prevent duplicate interaction
        }

        try {
            MongoCollection<Document> saveArticleCollection = database.getCollection("Saved_articles");

            Document userArticleDoc = saveArticleCollection.find(Filters.eq("username", Username)).first();
            if (userArticleDoc == null) {
                userArticleDoc = new Document("username", Username)
                        .append("Saved", new ArrayList<String>())
                        .append("Liked", new ArrayList<String>())
                        .append("Disliked", new ArrayList<String>())
                        .append("Rated", new ArrayList<String>());
                saveArticleCollection.insertOne(userArticleDoc);
            }
            saveArticleCollection.updateOne(
                    Filters.eq("username", Username),
                    Updates.addToSet(interactionType, articleTitle)
            );

            System.out.println("User " + Username + " " + interactionType + " the article: " + articleTitle);
        } catch (Exception e) {
            System.err.println("Failed to save user interaction: " + e.getMessage());
        }
    }


    @FXML
    private void saveArticles() {
        if (validateArticleAndCategory()) {
            articleInteractionSave("Saved", articleTitle);
            save_btn.setDisable(true);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> items = FXCollections.observableArrayList("Not Bad", "Good", "Very Good", "Excellent", "Outstanding");
        rate_combobox.setItems(items);
    }
}
