package com.example.course;

/**
 * The Main class for the JavaFX application.
 */

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HelloApplication extends Application {
    // Variables to store the initial mouse click coordinates for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * The main entry point for the JavaFX application.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Load the FXML file for the Sign In screen
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));

        // Set the stage style to TRANSPARENT to create a custom-styled window
        stage.initStyle(StageStyle.TRANSPARENT);

        // Event handler for handling mouse press to initiate window dragging
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        // Event handler for handling mouse drag to move the window
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Create a scene with the loaded FXML content and set it on the stage
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // Display the stage
        stage.show();
    }

    /**
     * The main method, launching the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}