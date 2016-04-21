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
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Author;
import temperatus.model.service.AuthorService;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View to create and save a new author
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

    private static Logger logger = LoggerFactory.getLogger(NewAuthorController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        author = null;
        translate();
    }

    /**
     * When editing an author, pre-load its data
     *
     * @param author author to update/edit
     */
    public void setAuthorForUpdate(Author author) {
        saveButton.setText(language.get(Lang.UPDATE));  // change save button text to update
        this.author = author;
        nameInput.setText(author.getName());
    }

    /**
     * Save or update an author on the DB
     */
    @Override
    @FXML
    void save() {
        try {
            logger.info("Saving author...");

            if (author == null) {   // creation of new author - no update
                author = new Author();
            }

            author.setName(nameInput.getText());
            authorService.saveOrUpdate(author);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new author creation
                VistaNavigator.getController().reload(author);
            }

            logger.info("Saved: " + author);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Invalid name");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_NAME));
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEWAUTHOR));
        nameInput.setPromptText(language.get(Lang.NAMEPROMPT));
        nameLabel.setText(language.get(Lang.NAMELABEL));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
    }
}
