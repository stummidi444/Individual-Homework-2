package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import application.Question;

public class QAPage {
    private final DatabaseHelper databaseHelper;
    private final User user;
    private final ObservableList<Question> questionList = FXCollections.observableArrayList();

    public QAPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: black;");

        Label titleLabel = new Label("Student Q&A Platform");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        // TableView to display questions
        TableView<Question> questionTable = new TableView<>();
        questionTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY); // Default resizing behavior

        // Column for displaying the username
        TableColumn<Question, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        // Column for displaying questions
        TableColumn<Question, String> questionColumn = new TableColumn<>("Question");
        questionColumn.setCellValueFactory(cellData -> cellData.getValue().questionProperty());

        questionTable.getColumns().addAll(userColumn, questionColumn);
        questionTable.setItems(questionList);
        fetchQuestions();

        // Input field for posting a new question
        TextField questionInputField = new TextField();
        questionInputField.setPromptText("Enter your question...");

        // Button to submit a new question
        Button postButton = new Button("Post Question");
        postButton.setOnAction(e -> {
            String questionText = questionInputField.getText().trim();
            if (!questionText.isEmpty()) {
                try {
                    databaseHelper.addQuestion(user.getUserName(), questionText);
                    fetchQuestions(); // Refresh UI
                    questionInputField.clear();
                } catch (SQLException ex) {
                    displayAlert("Failed to post question!", Alert.AlertType.ERROR);
                }
            }
        });

        // Button to edit selected question
        Button editButton = new Button("✏ Edit Selected Question");
        editButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null && selectedQuestion.getUsername().equals(user.getUserName())) {
                modifyQuestion(selectedQuestion);
            } else {
                displayAlert("You are only allowed to edit your own posts!", Alert.AlertType.WARNING);
            }
        });

        // Button to remove selected question
        Button removeButton = new Button("❌ Remove Selected Question");
        removeButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null && selectedQuestion.getUsername().equals(user.getUserName())) {
                removeQuestion(selectedQuestion);
            } else {
                displayAlert("You can only remove your own posts!", Alert.AlertType.WARNING);
            }
        });

        // Button to view answers
        Button answersButton = new Button("Check Answers");
        answersButton.setOnAction(e -> {
            Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
            if (selectedQuestion != null) {
                new AnswerPage(databaseHelper, user, selectedQuestion.getQuestion()).show(primaryStage);
            } else {
                displayAlert("Select a question first!", Alert.AlertType.WARNING);
            }
        });

        // Back button to return to the user dashboard
        Button backButton = new Button("⬅ Return to Home");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #555555; -fx-text-fill: white;");
        backButton.setOnAction(e -> new UserHomePage(databaseHelper, user).show(primaryStage));

        layout.getChildren().addAll(titleLabel, questionTable, questionInputField, postButton, editButton, removeButton, answersButton, backButton);

        Scene scene = new Scene(layout, 700, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Q&A System");
        primaryStage.show();
    }

    // Fetch questions from the database and update the table
    private void fetchQuestions() {
        try {
            questionList.clear();
            List<Question> questions = databaseHelper.getAllQuestions();
            questionList.addAll(questions);
        } catch (SQLException e) {
            displayAlert("Error retrieving questions!", Alert.AlertType.ERROR);
        }
    }

    // Modify an existing question
    private void modifyQuestion(Question question) {
        TextInputDialog dialog = new TextInputDialog(question.getQuestion());
        dialog.setTitle("Edit Post");
        dialog.setHeaderText("Update your question:");
        dialog.setContentText("Revised Question:");

        dialog.showAndWait().ifPresent(newQuestion -> {
            if (!newQuestion.isEmpty()) {
                try {
                    databaseHelper.updateQuestion(user.getUserName(), question.getQuestion(), newQuestion);
                    fetchQuestions(); // Refresh UI
                } catch (SQLException ex) {
                    displayAlert("Error updating question!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // Remove a question from the system
    private void removeQuestion(Question question) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText("Are you sure you want to delete this post?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    databaseHelper.deleteQuestion(user.getUserName(), question.getQuestion());
                    fetchQuestions(); // Refresh UI
                } catch (SQLException ex) {
                    displayAlert("Failed to delete question!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // Display alerts to users
    private void displayAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
