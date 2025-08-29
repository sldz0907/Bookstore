package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.sql.Connection;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dao.connectDB;

public class login_controller {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button exitButton;
    
    // DB TOOLS
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    
    private void loadScene(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    public void handleLogin(ActionEvent event) {
    	String username = usernameField.getText();
    	String password = passwordField.getText();
    	
    	if(username.isEmpty() || password.isEmpty()) {
    		showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
    		return;
    	}
    	
    	String sql = "SELECT role FROM Accounts WHERE username = ? AND password_hash = ? AND is_active = 1";
    	
    	try {
    		connect = connectDB.getConnection();
    		prepare = connect.prepareStatement(sql);
    		prepare.setString(1, username);
    		prepare.setString(2, password);
    		
    		result = prepare.executeQuery();
    		
            if (result.next()) {
                String role = result.getString("role");

                if (role.equals("manager")) {
                    loadScene("/views/fxml/manager.fxml");
                } else if (role.equals("customer")) {
                	showAlert(Alert.AlertType.ERROR, "Login", "Login as customer.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Unable to login.");
            }

    	} catch (Exception e) {
            e.printStackTrace();
        } finally {
        	try {
        		if (result != null) result.close();
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        } 
    }
    
    @FXML
    public void handleExit(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}
