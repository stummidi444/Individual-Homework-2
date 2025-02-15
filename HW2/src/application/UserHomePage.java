package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class UserHomePage {
    
    private final DatabaseHelper databaseHelper; // Instance of DatabaseHelper
    private final User user; // Current user information

    public UserHomePage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper; // Initialize databaseHelper
        this.user = user; // Initialize user
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);  // Increased spacing for better layout
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to greet the user
        Label userLabel = new Label("Hello, " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Back button to navigate to the Welcome Page
        Button backButton = new Button("⬅ Back to Welcome Page");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new WelcomeLoginPage(databaseHelper).show(primaryStage, user)); 

        // Add "Q&A System" button for student users only
        if (user.getRole().contains("student")) {
            Button qaButton = new Button("Go to Q&A Page");
            qaButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
            qaButton.setOnAction(e -> new QAPage(databaseHelper, user).show(primaryStage));  // Navigate to Q&A Page
            layout.getChildren().add(qaButton);  // Display button only for student users
        }

        layout.getChildren().addAll(userLabel, backButton);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
