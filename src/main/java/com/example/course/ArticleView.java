package com.example.course;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ArticleView {

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

    String Username = UserPage.username;

    private MongoDatabase database = MongoClients.create("mongodb://localhost:27017").getDatabase("News_Recommendation");

    public void displayContent(String title, String content, String category) {
        try{
            this.category = category;
            File file = new File("src/main/resources/com/example/course/web.html");
            String html = new String(Files.readAllBytes(file.toPath()));

            html = html.replace("<!-- title -->", title);
            html = html.replace("<!--content -->", content);

            WebEngine web = webView.getEngine();
            web.loadContent(html);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void CategoryUpdatePoints(int points){
        if(category == null || category.isEmpty()){
            System.out.println("Category is empty");
            return;
        }

        MongoCollection<Document> articlecollection = database.getCollection("Articlepoints");

        articlecollection.updateOne(
                Filters.eq("username", Username),
                Updates.inc(category, points)
        );
        System.out.println((points > 0 ? "LIKED" : "DISLIKED") + "category:" + category +
                "for user:" + Username + "Upadted by" + points + "points");
    }

    private void userInteractionCheck(String title){
        MongoCollection<Document> favArticlecollection = database.getCollection("Articlepoints");

        Document userArticleDoc = favArticlecollection.find(Filters.eq("username", Username)).first();

        if(userArticleDoc == null){
            if(userArticleDoc.getList("Saved", String.class).contains(title)){
                save_btn.setDisable(true);
            }
            if(userArticleDoc.getList("Liked", String.class).contains(title) ||
                    userArticleDoc.getList("Disliked", String.class).contains(title)){
                like_btn.setDisable(true);
                dislike_btn.setDisable(true);
            }
            if(userArticleDoc.getList("Rated", String.class).contains(title)){
                rate_combobox.setDisable(true);
                rate_btn.setDisable(true);

            }
        }
    }

    private void articleInteractionSave(String interactionType, String articleTitle){
        MongoCollection<Document> saveArticlecollection = database.getCollection("Saved_articles");

        Document userArticleDoc = saveArticlecollection.find(Filters.eq("username", Username)).first();
        if(userArticleDoc == null){
            userArticleDoc = new Document("username", Username)
                      .append("Saved", new ArrayList<String>())
                      .append("Liked", new ArrayList<String>())
                      .append("Disliked", new ArrayList<String>())
                      .append("Rated", new ArrayList<String>());
            saveArticlecollection.insertOne(userArticleDoc);
        }
        saveArticlecollection.updateOne(
                Filters.eq("username", Username),
                Updates.addToSet(interactionType, articleTitle)
        );

        System.out.println("User" + Username + " "+ interactionType + "the article" + articleTitle);
    }
}
