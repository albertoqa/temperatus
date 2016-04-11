package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.model.pojo.Author;
import temperatus.model.service.AuthorService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * View to create and save a new project
 * <p>
 * Created by alberto on 19/1/16.
 */
@Controller
@Scope("prototype")
public class NewAuthorController extends AbstractCreationController implements Initializable {

    @FXML private Label nameLabel;
    @FXML private TextField nameInput;

    @Autowired AuthorService authorService;
    private Author author;

    static Logger logger = LoggerFactory.getLogger(NewAuthorController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        author = null;
        translate();
    }

    public void setAuthorForUpdate(Author author) {
        saveButton.setText(language.get(Constants.UPDATE));
        this.author = author;
        nameInput.setText(author.getName());
    }

    /**
     * Save a new author on the DB
     */
    @Override
    @FXML
    void save() {
        String name;
        Date startDate;
        String observations;

        try {
            logger.info("Saving author...");

            name = nameInput.getText();

            if(author == null) {
                author = new Author();
            }

            author.setName(name);

            authorService.saveOrUpdate(author);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new project creation
                VistaNavigator.getController().reload(author);
            }

            logger.info("Saved: " + author);

        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, "Duplicate entry");
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWAUTHOR));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
    }
}
