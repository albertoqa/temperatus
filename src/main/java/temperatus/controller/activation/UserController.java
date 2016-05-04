package temperatus.controller.activation;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Author;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.model.service.AuthorService;
import temperatus.util.User;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Ask for who is going to use the application to make a history of use.
 * <p>
 * Created by alberto on 3/5/16.
 */
@Controller
public class UserController implements Initializable, AbstractController {

    @FXML private AnchorPane anchorPane;
    @FXML private ComboBox<Author> userBox;
    @FXML private TextField userInput;
    @FXML private Label questionLabel;
    @FXML private Button startButton;
    @FXML private Button newUserButton;

    @Autowired AuthorService authorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new AutoCompleteComboBoxListener<>(userBox);
        translate();

        getAllElements();
    }

    /**
     * Fetch all Authors from database and add it to the box.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Author>> getAuthorsTask = new Task<List<Author>>() {
            @Override
            public List<Author> call() throws Exception {
                return authorService.getAll();
            }
        };

        // on task completion add all authors to the table
        getAuthorsTask.setOnSucceeded(e -> {
            userBox.getItems().addAll(getAuthorsTask.getValue());

            if(User.getUser() != null) {
                userBox.getSelectionModel().select(User.getUser());
            }

        });

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getAuthorsTask);
    }

    /**
     * Set the user currently using the application
     */
    @FXML
    private void setUser() {
        if (userBox.isVisible()) {
            Author selected = userBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                User.setUser(selected);
                VistaNavigator.closeModal(anchorPane);
            } else {
                showAlert(Alert.AlertType.INFORMATION, language.get(Lang.MUST_SELECT_USER));
            }
        } else {
            Author author = new Author();
            author.setName(userInput.getText());
            try {
                authorService.saveOrUpdate(author);
                User.setUser(author);
                VistaNavigator.closeModal(anchorPane);
            } catch (Exception e) {
                showAlert(Alert.AlertType.INFORMATION, language.get(Lang.INVALID_NAME));
            }
        }
    }

    /**
     * Change view elements to allow to insert a new user or select users
     */
    @FXML
    private void newUser() {
        if (userBox.isVisible()) {
            questionLabel.setText(language.get(Lang.INSERT_USER_NAME));
            newUserButton.setText(language.get(Lang.BACK_BUTTON));
            userBox.setVisible(false);
            userInput.setVisible(true);
        } else {
            questionLabel.setText(language.get(Lang.WHO_IS_USING));
            newUserButton.setText(language.get(Lang.NEW_USER_BUTTON));
            userBox.setVisible(true);
            userInput.setVisible(false);
        }
    }

    @Override
    public void translate() {
        newUserButton.setText(language.get(Lang.NEW_USER_BUTTON));
        userInput.setPromptText(language.get(Lang.NEW_USER_PROMPT));
        startButton.setText(language.get(Lang.CONTINUE));
    }

}
