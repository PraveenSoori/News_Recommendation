package com.example.course;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminPage {

    @FXML
    private AnchorPane Admin_page;

    @FXML
    private Button admin_Log_Out;

    @FXML
    private Button admin_Manage_User;

    @FXML
    private Button admin_Manage_artical;

    @FXML
    private Button admin_Profile;

    @FXML
    private Button admin_User_Interface;


    public void setUserData() {
    }

    @FXML
    private void handleAdminLogOut(ActionEvent event) {
        try {
            // Load the user-page.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent userPageRoot = loader.load();

            // Set the new scene
            Scene scene = new Scene(userPageRoot);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, display an alert if the page cannot be loaded
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load the user page.");
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
