package com.example.course;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class HelloController {

    @FXML
    private TextField Username_text;

    @FXML
    private TextField password_text;

    @FXML
    void control_signin(ActionEvent event) {
            String demouser="username";
            String demopassword="password";
            String username = Username_text.getText();
            String password = password_text.getText();

            if (username.equals(demouser) && password.equals(demopassword)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Login");
                alert.setHeaderText(null);
                alert.setContentText("Welcome " + username);
                alert.showAndWait();

            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login");
                alert.setHeaderText(null);
                alert.setContentText("Incorrect username or password");
                alert.showAndWait();

            }
    }

    }

