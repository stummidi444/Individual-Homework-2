package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

public class AnswerPage {
    private final DatabaseHelper databaseHelper;
    private final User user;
    private final String question;
    private final ObservableList<String> answerList = FXCollections.observableArrayList();

    public AnswerPage(DatabaseHelper databaseHelper, User user, String question) {
        this.databaseHelper = databaseHelper;
        this.user = user;
        this.question = question;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label titleLabel = new Label("Replies for: " + question);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // List view to display replies
        ListView<String> answerListView = new ListView<>(answerList);
        answerListView.setPrefHeight(250);
        fetchAnswers();

        // Input field for adding new replies
        TextField newAnswerField = new TextField();
        newAnswerField.setPromptText("Enter your reply here...");

        // Button to submit a new reply
        Button postButton = new Button("Post Reply");
        postButton.setOnAction(e -> {
            String reply = newAnswerField.getText().trim();
            if (!reply.isEmpty()) {
                try {
                    databaseHelper.addAnswer(question, user.getUserName(), reply);
                    fetchAnswers(); // Refresh UI
                    newAnswerField.clear();
                } catch (SQLException ex) {
                    displayAlert("Failed to submit reply!", Alert.AlertType.ERROR);
                }
            }
        });

        // Button to mark an answer as resolved
        Button resolveButton = new Button("Resolve Question");
        resolveButton.setOnAction(e -> {
            String selectedReply = answerListView.getSelectionModel().getSelectedItem();
            if (selectedReply != null) {
                try {
                    databaseHelper.markAnswerAsResolved(question, selectedReply);
                    displayAlert("✅ Question resolved!", Alert.AlertType.INFORMATION);
                    fetchAnswers(); // Refresh UI
                } catch (SQLException ex) {
                    displayAlert("Error marking as resolved!", Alert.AlertType.ERROR);
                }
            } else {
                displayAlert("Select a reply to mark as resolved!", Alert.AlertType.WARNING);
            }
        });

        // Button to edit a reply
        Button editButton = new Button("Modify Reply");
        editButton.setOnAction(e -> {
            String selectedReply = answerListView.getSelectionModel().getSelectedItem();
            if (selectedReply != null && selectedReply.startsWith(user.getUserName() + ": ")) {
                TextInputDialog dialog = new TextInputDialog(selectedReply.split(": ")[1]);
                dialog.setTitle("Edit Reply");
                dialog.setHeaderText("Update your reply:");
                dialog.setContentText("New Reply:");
                dialog.showAndWait().ifPresent(newReply -> {
                    if (!newReply.isEmpty()) {
                        try {
                            databaseHelper.updateAnswer(user.getUserName(), selectedReply.split(": ")[1], newReply);
                            fetchAnswers(); // Refresh UI
                        } catch (SQLException ex) {
                            displayAlert("Error updating reply!", Alert.AlertType.ERROR);
                        }
                    }
                });
            } else {
                displayAlert("You can only edit your own replies!", Alert.AlertType.WARNING);
            }
        });

        // Button to delete a reply
        Button deleteButton = new Button("Remove Reply");
        deleteButton.setOnAction(e -> {
            String selectedReply = answerListView.getSelectionModel().getSelectedItem();
            if (selectedReply != null && selectedReply.startsWith(user.getUserName() + ": ")) {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Delete Confirmation");
                confirmDialog.setHeaderText("Are you sure you want to remove this reply?");
                confirmDialog.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            databaseHelper.deleteAnswer(user.getUserName(), selectedReply.split(": ")[1]);
                            fetchAnswers(); // Refresh UI
                        } catch (SQLException ex) {
                            displayAlert("Failed to delete reply!", Alert.AlertType.ERROR);
                        }
                    }
                });
            } else {
                displayAlert("You can only remove your own replies!", Alert.AlertType.WARNING);
            }
        });

        // Back button to return to QAPage
        Button backButton = new Button("⬅ Return to Questions");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new QAPage(databaseHelper, user).show(primaryStage));

        layout.getChildren().addAll(titleLabel, answerListView, newAnswerField, postButton, resolveButton, editButton, deleteButton, backButton);

        Scene scene = new Scene(layout, 600, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reply Page");
        primaryStage.show();
    }

    // Fetch replies from database
    private void fetchAnswers() {
        try {
            answerList.clear();
            List<String> replies = databaseHelper.getAnswersForQuestion(question);
            answerList.addAll(replies);
        } catch (SQLException e) {
            displayAlert("Error retrieving replies!", Alert.AlertType.ERROR);
        }
    }

    // Display alerts to users
    private void displayAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
