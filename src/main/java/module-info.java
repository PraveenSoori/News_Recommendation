module com.example.course {
    requires javafx.fxml;
    requires java.sql;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires org.json;
    requires javafx.web;
    requires java.desktop;


    opens com.example.course to javafx.fxml;
    exports com.example.course;
}