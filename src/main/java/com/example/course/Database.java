package com.example.course;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Database {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> adminCollection;
    private final MongoCollection<Document> loginCollection;
    private final MongoCollection<Document> articlePointsCollection;
    private final MongoCollection<Document> articleCollection;

    public Database(String uri, String dbName) {
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase(dbName);
        userCollection = database.getCollection("User_Detail");
        adminCollection = database.getCollection("admin_details");
        loginCollection = database.getCollection("User_login_Detail");
        articlePointsCollection = database.getCollection("Articlepoints");
        articleCollection = database.getCollection("my_articals");
    }

    public Member getMember(String username) {
        // Fetch from the User collection
        Document doc = userCollection.find(Filters.eq("Username", username)).first();
        if (doc != null) {
            return new User(
                    doc.getString("Username"),
                    doc.getString("Password"),
                    doc.getString("Email"),
                    doc.getString("Full_Name"),
                    doc.getInteger("Age"),
                    doc.getList("Interests", String.class)
            );
        }

        // Fetch from the Admin collection
        doc = adminCollection.find(Filters.eq("Username", username)).first();
        if (doc != null) {
            return new Admin(
                    doc.getString("Username"),
                    doc.getString("Password"),
                    doc.getString("Email"),
                    doc.getString("Full_Name"),
                    doc.getInteger("Age", 0) // Default age to 0 if not provided
            );
        }

        // If no match found, return null
        return null;
    }


    public void addArticle(Article article) {
        Document doc = new Document("title", article.getTitle()).append("content", article.getContent());
        articleCollection.insertOne(doc);
    }

    public void deleteArticle(String title) {
        articleCollection.deleteOne(Filters.eq("title", title));
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (Document doc : userCollection.find()) {
            users.add(new User(
                    doc.getString("Username"),
                    doc.getString("Password"),
                    doc.getString("Email"),
                    doc.getString("Full_Name"),
                    doc.getInteger("Age"),
                    doc.getList("Interests", String.class)
            ));
        }
        return users;
    }

    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();
        for (Document doc : articleCollection.find()) {
            articles.add(new Article(doc.getString("title"), doc.getString("content")));
        }
        return articles;
    }

    public void saveLoginDetails(String username) {
        Document loginDetail = new Document("Username", username)
                .append("Login_time", java.time.LocalDateTime.now().toString());
        loginCollection.insertOne(loginDetail);
    }

    public void registerUser(User user) {
        Document newUser = new Document("Username", user.getUsername())
                .append("Password", user.getPassword())
                .append("Email", user.getEmail())
                .append("Full_Name", user.getFullName())
                .append("Age", user.getFullName())
                .append("Interests", user.getInterests());

        Document userPreferences = new Document("Username", user.getUsername());
        for (String category : new String[]{"Entertainment", "Tech", "Lifestyle and Culture", "Sport", "Politics", "Science"}) {
            userPreferences.append(category, user.getInterests().contains(category) ? 5 : 0);
        }

        articlePointsCollection.insertOne(userPreferences);
        userCollection.insertOne(newUser);
    }

    public MongoCollection<Document> getAdminCollection() {
        return adminCollection;
    }

    public boolean deleteUser(String username) {
        Document deletedUser = userCollection.findOneAndDelete(Filters.eq("Username", username));
        return deletedUser != null;
    }

    public boolean checkIfAdmin(String username, String password) {
        Document adminDoc = adminCollection.find(Filters.and(Filters.eq("Username", username), Filters.eq("Password", password))).first();
        return adminDoc != null;
    }

    public boolean checkCredentials(String username, String password) {
        Document userDoc = userCollection.find(Filters.and(Filters.eq("Username", username), Filters.eq("Password", password))).first();
        return userDoc != null;
    }
}
